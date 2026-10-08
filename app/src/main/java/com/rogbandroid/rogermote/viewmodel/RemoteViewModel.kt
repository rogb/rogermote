package com.rogbandroid.rogermote.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rogbandroid.rogermote.data.TvPreferences
import com.rogbandroid.rogermote.data.SavedTvDevices
import com.rogbandroid.rogermote.data.TvDevice
import com.rogbandroid.rogermote.data.TvApplication
import com.rogbandroid.rogermote.discovery.SamsungSsdpDiscovery
import com.rogbandroid.rogermote.tv.ConnectionState
import com.rogbandroid.rogermote.tv.RemoteCommand
import com.rogbandroid.rogermote.tv.TvRemoteClient
import com.rogbandroid.rogermote.tv.samsung.KeystoreSamsungTokenStore
import com.rogbandroid.rogermote.tv.samsung.SamsungRemoteClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class RemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val tvPreferences = TvPreferences(application.applicationContext)
    private val tvDiscovery = SamsungSsdpDiscovery(application.applicationContext)
    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    private val tokenStore = KeystoreSamsungTokenStore(application.applicationContext)
    private val remoteClient: TvRemoteClient = SamsungRemoteClient(
        tokenStore = tokenStore,
    )
    private val savedIpAddress = tvPreferences.readIpAddress()
    private val mutableUiState = MutableStateFlow(
        RemoteUiState(
            ipAddress = savedIpAddress.orEmpty(),
            hasSavedConfiguration = savedIpAddress != null,
            hapticsEnabled = tvPreferences.readHapticsEnabled(),
            discoveredDevices = tvPreferences.readDiscoveredDevices(),
            page = if (savedIpAddress != null) RemotePage.Remote else RemotePage.Setup,
        ),
    )
    private var reconnectJob: Job? = null
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            mutableUiState.update { it.copy(isNetworkAvailable = true) }
            scheduleReconnect()
        }

        override fun onLost(network: Network) {
            mutableUiState.update { it.copy(isNetworkAvailable = false) }
            if (mutableUiState.value.isConnected) remoteClient.disconnect()
        }
    }
    val uiState: StateFlow<RemoteUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            remoteClient.connectionState.collect { connectionState ->
                mutableUiState.update {
                        it.copy(
                            connectionState = connectionState,
                            installedApplications = orderApplications(remoteClient.installedApplications.value),
                        page = if (connectionState == ConnectionState.Connected) {
                            RemotePage.Remote
                        } else {
                            it.page
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            remoteClient.installedApplications.collect { applications ->
                mutableUiState.update { it.copy(installedApplications = orderApplications(applications)) }
            }
        }
        runCatching { connectivityManager?.registerDefaultNetworkCallback(networkCallback) }
        if (savedIpAddress != null) connect()
    }

    fun updateIpAddress(value: String) {
        mutableUiState.update { it.copy(ipAddress = value) }
    }

    fun connect() {
        val ipAddress = mutableUiState.value.ipAddress.trim()
        if (!ipAddress.isValidIpv4Address()) {
            mutableUiState.update {
                it.copy(
                    connectionState = ConnectionState.ConnectionFailed(
                        "Enter a valid IPv4 address, such as 192.168.1.50.",
                    ),
                )
            }
            return
        }

        tvPreferences.writeIpAddress(ipAddress)
        mutableUiState.update {
            it.copy(
                ipAddress = ipAddress,
                hasSavedConfiguration = true,
            )
        }
        remoteClient.connect(ipAddress)
    }

    fun disconnect() {
        remoteClient.disconnect()
    }

    fun sendCommand(command: RemoteCommand) {
        remoteClient.sendCommand(command)
    }

    fun launchApplication(application: TvApplication) {
        remoteClient.launchApplication(application)
    }

    fun reorderApplications(applications: List<TvApplication>) {
        val orderedApplications = applications.distinctBy { it.id }
        tvPreferences.writeShortcutOrder(orderedApplications.map { it.id })
        mutableUiState.update { it.copy(installedApplications = orderedApplications) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        tvPreferences.writeHapticsEnabled(enabled)
        mutableUiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun onAppForeground() {
        val savedIpAddress = tvPreferences.readIpAddress()
        if (savedIpAddress == null && mutableUiState.value.hasSavedConfiguration) {
            mutableUiState.update {
                it.copy(
                    ipAddress = "",
                    hasSavedConfiguration = false,
                    page = RemotePage.Setup,
                )
            }
        }
        scheduleReconnect()
    }

    fun onAppBackground() {
        reconnectJob?.cancel()
        // Keep the authenticated WebSocket alive across ordinary activity backgrounding.
        // Reopening the app should not create a new Samsung authorization handshake.
    }

    fun openSetup() {
        mutableUiState.update { it.copy(page = RemotePage.Setup) }
    }

    fun openRemote() {
        if (mutableUiState.value.hasSavedConfiguration || mutableUiState.value.isConnected) {
            mutableUiState.update { it.copy(page = RemotePage.Remote) }
        }
    }

    fun scanForTvs() {
        if (mutableUiState.value.isDiscovering) return
        mutableUiState.update {
            it.copy(
                isDiscovering = true,
                discoveryError = null,
                onlineTvAddresses = emptySet(),
            )
        }
        viewModelScope.launch {
            val devices = runCatching { tvDiscovery.scan() }
            val savedDevices = devices.getOrNull()?.let { found ->
                SavedTvDevices.merge(mutableUiState.value.discoveredDevices, found).also {
                    tvPreferences.writeDiscoveredDevices(it)
                }
            }
            mutableUiState.update { state ->
                devices.fold(
                    onSuccess = { found ->
                        state.copy(
                            discoveredDevices = savedDevices ?: state.discoveredDevices,
                            onlineTvAddresses = found.mapTo(mutableSetOf()) { it.ipAddress },
                            isDiscovering = false,
                            discoveryError = if (found.isEmpty()) {
                                if (state.discoveredDevices.isEmpty()) {
                                    "No Samsung TVs found. Enter an IP address manually."
                                } else {
                                    "No Samsung TVs found in this scan. Previously found TVs are still listed."
                                }
                            } else {
                                null
                            },
                        )
                    },
                    onFailure = {
                        state.copy(
                            isDiscovering = false,
                            discoveryError = "TV scan failed. Enter an IP address manually.",
                        )
                    },
                )
            }
        }
    }

    fun selectTv(device: TvDevice) {
        if (mutableUiState.value.isConnectionInProgress) return
        reconnectJob?.cancel()
        mutableUiState.update {
            it.copy(
                ipAddress = device.ipAddress,
                discoveryError = null,
                page = RemotePage.Setup,
            )
        }
        connect()
        if (remoteClient.connectionState.value == ConnectionState.Connected) {
            openRemote()
        }
    }

    fun removeDiscoveredTv(device: TvDevice) {
        if (mutableUiState.value.isDiscovering) return
        val remaining = mutableUiState.value.discoveredDevices.filterNot { it == device }
        tvPreferences.writeDiscoveredDevices(remaining)
        mutableUiState.update { it.copy(discoveredDevices = remaining) }
    }

    fun updateTvAlias(device: TvDevice, alias: String) {
        val devices = mutableUiState.value.discoveredDevices.map {
            if (it.ipAddress == device.ipAddress) it.copy(alias = alias) else it
        }
        tvPreferences.writeDiscoveredDevices(devices)
        mutableUiState.update { it.copy(discoveredDevices = devices) }
    }

    fun forgetSavedTv() {
        val ipAddress = mutableUiState.value.ipAddress.trim()
        remoteClient.disconnect()
        if (ipAddress.isNotEmpty()) tokenStore.clear(ipAddress)
        tvPreferences.clear()
        mutableUiState.value = RemoteUiState(
            hapticsEnabled = tvPreferences.readHapticsEnabled(),
            discoveredDevices = tvPreferences.readDiscoveredDevices(),
        )
    }

    override fun onCleared() {
        reconnectJob?.cancel()
        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
        remoteClient.disconnect()
        super.onCleared()
    }

    private fun scheduleReconnect() {
        val state = mutableUiState.value
        if (!state.hasSavedConfiguration || !state.isNetworkAvailable || state.isConnected) return
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            delay(RECONNECT_DELAY_MILLIS)
            val currentState = mutableUiState.value
            if (currentState.hasSavedConfiguration &&
                currentState.isNetworkAvailable &&
                !currentState.isConnected &&
                !currentState.isConnectionInProgress
            ) {
                connect()
            }
        }
    }

    private fun orderApplications(applications: List<TvApplication>): List<TvApplication> {
        val savedOrder = tvPreferences.readShortcutOrder()
        if (savedOrder.isEmpty()) return applications
        val byId = applications.associateBy { it.id }
        val savedApplications = savedOrder.mapNotNull(byId::get)
        val savedIds = savedApplications.mapTo(mutableSetOf()) { it.id }
        return savedApplications + applications.filterNot { it.id in savedIds }
    }

    private companion object {
        const val RECONNECT_DELAY_MILLIS = 750L
    }
}

internal fun String.isValidIpv4Address(): Boolean {
    val parts = split('.')
    return parts.size == 4 && parts.all { part ->
        part.isNotEmpty() &&
            part.length <= 3 &&
            part.all(Char::isDigit) &&
            part.toIntOrNull() in 0..255
    }
}
