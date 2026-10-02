package com.rogbandroid.rogermote.tv.samsung

import java.util.Base64
import okhttp3.HttpUrl
import org.json.JSONObject

internal object SamsungProtocol {
    private const val REMOTE_PATH = "api/v2/channels/samsung.remote.control"
    private const val REMOTE_PORT = 8002

    fun remoteUrl(ipAddress: String, appName: String, token: String?): String {
        val encodedName = Base64.getEncoder().encodeToString(appName.toByteArray(Charsets.UTF_8))
        return HttpUrl.Builder()
            .scheme("https")
            .host(ipAddress)
            .port(REMOTE_PORT)
            .addPathSegments(REMOTE_PATH)
            .addQueryParameter("name", encodedName)
            .apply {
                if (!token.isNullOrBlank()) {
                    addQueryParameter("token", token)
                }
            }
            .build()
            .toString()
    }

    fun commandMessage(command: SamsungKey): String = JSONObject()
        .put("method", "ms.remote.control")
        .put(
            "params",
            JSONObject()
                .put("Cmd", "Click")
                .put("DataOfCmd", command.protocolValue)
                .put("Option", "false")
                .put("TypeOfRemote", "SendRemoteKey"),
        )
        .toString()

    fun parseEvent(message: String): SamsungEvent = runCatching {
        val root = JSONObject(message)
        when (root.optString("event")) {
            "ms.channel.connect" -> SamsungEvent.Connected(
                token = root.optJSONObject("data")
                    ?.optString("token")
                    ?.takeIf(String::isNotBlank),
            )
            "ms.channel.unauthorized" -> SamsungEvent.Unauthorized
            else -> SamsungEvent.Other
        }
    }.getOrDefault(SamsungEvent.Invalid)
}

internal sealed interface SamsungEvent {
    data class Connected(val token: String?) : SamsungEvent
    data object Unauthorized : SamsungEvent
    data object Other : SamsungEvent
    data object Invalid : SamsungEvent
}
