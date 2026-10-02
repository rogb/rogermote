package com.rogbandroid.rogermote.viewmodel

import com.rogbandroid.rogermote.data.TvDevice
import com.rogbandroid.rogermote.tv.ConnectionState

enum class RemotePage {
    Setup,
    Remote,
}

data class RemoteUiState(
    val ipAddress: String = "",
    val hasSavedConfiguration: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val discoveredDevices: List<TvDevice> = emptyList(),
    val isDiscovering: Boolean = false,
    val discoveryError: String? = null,
    val hapticsEnabled: Boolean = true,
    val isNetworkAvailable: Boolean = true,
    val page: RemotePage = RemotePage.Setup,
) {
    val isConnectionInProgress: Boolean
        get() = connectionState == ConnectionState.Connecting ||
            connectionState == ConnectionState.WaitingForTvApproval

    val isConnected: Boolean
        get() = connectionState == ConnectionState.Connected
}
