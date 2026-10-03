package com.rogbandroid.rogermote.tv.samsung

import android.util.Log
import android.util.Base64
import com.rogbandroid.rogermote.data.TvApplication
import com.rogbandroid.rogermote.tv.ConnectionState
import com.rogbandroid.rogermote.tv.RemoteCommand
import com.rogbandroid.rogermote.tv.TvRemoteClient
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

internal class SamsungRemoteClient(
    private val tokenStore: SamsungTokenStore,
) : TvRemoteClient {
    private val mutableConnectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = mutableConnectionState.asStateFlow()
    private val mutableInstalledApplications = MutableStateFlow<List<TvApplication>>(emptyList())
    override val installedApplications: StateFlow<List<TvApplication>> =
        mutableInstalledApplications.asStateFlow()

    private var webSocket: WebSocket? = null
    private var httpClient: OkHttpClient? = null
    private var connectionGeneration = 0
    private var currentIpAddress: String? = null
    private val pendingIconRequests = ArrayDeque<TvApplication>()
    private var activeIconRequest: TvApplication? = null

    override fun connect(ipAddress: String) {
        val normalizedIpAddress = ipAddress.trim()
        if (currentIpAddress == normalizedIpAddress &&
            mutableConnectionState.value in setOf(
                ConnectionState.Connecting,
                ConnectionState.WaitingForTvApproval,
                ConnectionState.Connected,
            )
        ) {
            Log.d(TAG, "Ignoring duplicate Samsung connection request for the active session")
            return
        }

        val savedToken = tokenStore.read(normalizedIpAddress)
        startConnection(
            ipAddress = normalizedIpAddress,
            token = savedToken,
            mayRetryWithoutToken = savedToken != null,
        )
    }

    private fun startConnection(
        ipAddress: String,
        token: String?,
        mayRetryWithoutToken: Boolean,
    ) {
        currentIpAddress = ipAddress
        disconnectCurrentSocket()
        val generation = ++connectionGeneration
        mutableConnectionState.value = ConnectionState.Connecting

        val client = createTvOnlyHttpClient(ipAddress)
        val request = Request.Builder()
            .url(SamsungProtocol.remoteUrl(ipAddress, APP_NAME, token))
            .build()

        httpClient = client
        Log.d(TAG, "Opening Samsung TV remote connection")
        webSocket = client.newWebSocket(
            request,
            listener(
                generation = generation,
                ipAddress = ipAddress,
                hasSavedToken = token != null,
                mayRetryWithoutToken = mayRetryWithoutToken,
            ),
        )
    }

    override fun disconnect() {
        connectionGeneration++
        disconnectCurrentSocket()
        mutableConnectionState.value = ConnectionState.Disconnected
        mutableInstalledApplications.value = emptyList()
        pendingIconRequests.clear()
        activeIconRequest = null
        Log.d(TAG, "Samsung TV remote disconnected")
    }

    override fun sendCommand(command: RemoteCommand) {
        if (mutableConnectionState.value != ConnectionState.Connected) return

        val samsungKey = SamsungCommandMapper.toSamsungKey(command)
        val accepted = webSocket?.send(SamsungProtocol.commandMessage(samsungKey)) == true
        if (accepted) {
            Log.d(TAG, "Samsung remote command queued: ${samsungKey.name}")
        } else {
            mutableConnectionState.value = ConnectionState.ConnectionFailed(
                "The command could not be sent. Reconnect to the TV.",
            )
        }
    }

    override fun refreshApplications() {
        if (mutableConnectionState.value != ConnectionState.Connected) return
        val accepted = webSocket?.send(SamsungProtocol.installedApplicationsMessage()) == true
        if (accepted) Log.d(TAG, "Requested installed Samsung TV applications")
    }

    override fun launchApplication(application: TvApplication) {
        if (mutableConnectionState.value != ConnectionState.Connected) return
        val accepted = webSocket?.send(SamsungProtocol.launchApplicationMessage(application.id)) == true
        if (accepted) Log.d(TAG, "Requested launch for discovered TV application: ${application.name}")
    }

    private fun listener(
        generation: Int,
        ipAddress: String,
        hasSavedToken: Boolean,
        mayRetryWithoutToken: Boolean,
    ) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (generation != connectionGeneration) return
            mutableConnectionState.value = if (hasSavedToken) {
                ConnectionState.Connecting
            } else {
                ConnectionState.WaitingForTvApproval
            }
            Log.d(TAG, "Samsung TV WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (generation != connectionGeneration) return

            when (val event = SamsungProtocol.parseEvent(text)) {
                is SamsungEvent.Connected -> {
                    event.token?.let { token ->
                        val saved = runCatching { tokenStore.write(ipAddress, token) }.isSuccess
                        if (!saved) {
                            mutableConnectionState.value = ConnectionState.ConnectionFailed(
                                "TV approval succeeded, but pairing could not be saved securely.",
                            )
                            Log.e(TAG, "Samsung pairing token storage failed")
                            webSocket.close(NORMAL_CLOSURE, null)
                            return
                        }
                    }
                    mutableConnectionState.value = ConnectionState.Connected
                    Log.d(TAG, "Samsung TV authorization completed")
                    refreshApplications()
                }
                SamsungEvent.Unauthorized -> {
                    if (mayRetryWithoutToken) {
                        retryWithoutRejectedToken(ipAddress)
                    } else {
                        mutableConnectionState.value = ConnectionState.AuthorizationRejected
                        Log.w(TAG, "Samsung TV authorization rejected")
                    }
                }
                is SamsungEvent.Applications -> {
                    mutableInstalledApplications.value = event.applications
                    pendingIconRequests.clear()
                    activeIconRequest = null
                    event.applications
                        .filter { !it.iconReference.isNullOrBlank() }
                        .forEach(pendingIconRequests::addLast)
                    requestNextApplicationIcon()
                    Log.d(TAG, "Samsung TV application discovery returned ${event.applications.size} entries")
                }
                is SamsungEvent.ApplicationIcon -> {
                    val application = activeIconRequest
                    val iconData = event.iconData
                    if (application != null && !iconData.isNullOrBlank()) {
                        if (iconData.isImageData()) {
                            updateApplicationIcon(application, iconData)
                            activeIconRequest = null
                            requestNextApplicationIcon()
                        } else {
                            downloadApplicationIcon(application, iconData)
                        }
                    } else {
                        activeIconRequest = null
                        requestNextApplicationIcon()
                    }
                }
                SamsungEvent.Invalid -> Log.w(TAG, "Ignored malformed Samsung TV response")
                SamsungEvent.Other -> Log.d(TAG, "Received an unrelated Samsung TV event")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (generation != connectionGeneration) return
            if (mutableConnectionState.value !is ConnectionState.ConnectionFailed &&
                mutableConnectionState.value != ConnectionState.AuthorizationRejected
            ) {
                mutableConnectionState.value = ConnectionState.Disconnected
            }
            Log.d(TAG, "Samsung TV WebSocket closed")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (generation != connectionGeneration) {
                response?.close()
                return
            }

            val authorizationFailed = response?.code == 401 || response?.code == 403
            if (authorizationFailed && mayRetryWithoutToken) {
                response.close()
                retryWithoutRejectedToken(ipAddress)
                return
            } else if (authorizationFailed) {
                mutableConnectionState.value = ConnectionState.AuthorizationRejected
            } else {
                mutableConnectionState.value = ConnectionState.ConnectionFailed(failureMessage(t))
            }
            response?.close()
            Log.w(TAG, "Samsung TV connection failed: ${t.javaClass.simpleName}")
        }
    }

    private fun requestNextApplicationIcon() {
        if (activeIconRequest != null || mutableConnectionState.value != ConnectionState.Connected) return
        val next = pendingIconRequests.removeFirstOrNull() ?: return
        val iconPath = next.iconReference ?: return requestNextApplicationIcon()
        activeIconRequest = next
        val accepted = webSocket?.send(SamsungProtocol.applicationIconMessage(iconPath)) == true
        if (!accepted) activeIconRequest = null
        if (!accepted) requestNextApplicationIcon()
    }

    private fun updateApplicationIcon(application: TvApplication, iconData: String) {
        mutableInstalledApplications.value = mutableInstalledApplications.value.map {
            if (it.id == application.id) it.copy(iconData = iconData) else it
        }
    }

    private fun downloadApplicationIcon(application: TvApplication, reference: String) {
        val ipAddress = currentIpAddress ?: run {
            activeIconRequest = null
            requestNextApplicationIcon()
            return
        }
        val url = when {
            reference.startsWith("http://$ipAddress") || reference.startsWith("https://$ipAddress") -> reference
            reference.startsWith("/") -> "http://$ipAddress:8001$reference"
            else -> {
                activeIconRequest = null
                requestNextApplicationIcon()
                return
            }
        }
        val client = httpClient
        if (client == null) {
            activeIconRequest = null
            requestNextApplicationIcon()
            return
        }
        client.newCall(Request.Builder().url(url).build()).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                activeIconRequest = null
                requestNextApplicationIcon()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val bytes = it.body?.bytes()
                        if (bytes != null && bytes.isNotEmpty()) {
                            updateApplicationIcon(
                                application,
                                "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP),
                            )
                        }
                    }
                }
                activeIconRequest = null
                requestNextApplicationIcon()
            }
        })
    }

    private fun String.isImageData(): Boolean = startsWith("data:image/") ||
        runCatching {
            Base64.decode(substringAfter("base64,", this), Base64.DEFAULT).isNotEmpty()
        }.getOrDefault(false)

    private fun retryWithoutRejectedToken(ipAddress: String) {
        tokenStore.clear(ipAddress)
        Log.w(TAG, "Stored Samsung authorization was rejected; requesting approval again")
        startConnection(
            ipAddress = ipAddress,
            token = null,
            mayRetryWithoutToken = false,
        )
    }

    private fun disconnectCurrentSocket() {
        webSocket?.close(NORMAL_CLOSURE, "User disconnected")
        webSocket = null
        httpClient?.dispatcher?.cancelAll()
        httpClient?.connectionPool?.evictAll()
        httpClient = null
    }

    private fun failureMessage(error: Throwable): String = when (error) {
        is SocketTimeoutException -> "Connection timed out. Check the TV IP and that the TV is on."
        is ConnectException, is NoRouteToHostException ->
            "Could not reach the TV. Check its IP address and Wi-Fi connection."
        is SSLException -> "Could not establish a secure connection to the TV."
        else -> "Connection failed. Check the TV IP and that both devices are on the same network."
    }

    private fun createTvOnlyHttpClient(expectedHost: String): OkHttpClient {
        val trustManager = SamsungTvTrustManager()
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, _ -> hostname == expectedHost }
            .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private class SamsungTvTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
            throw CertificateException("Client certificates are not accepted")
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
            if (chain.isEmpty()) throw CertificateException("The TV supplied no certificate")
            chain.forEach(X509Certificate::checkValidity)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private companion object {
        const val TAG = "RogermoteSamsung"
        const val APP_NAME = "Rogermote"
        const val NORMAL_CLOSURE = 1000
        const val CONNECTION_TIMEOUT_SECONDS = 8L
        const val PING_INTERVAL_SECONDS = 30L
    }
}
