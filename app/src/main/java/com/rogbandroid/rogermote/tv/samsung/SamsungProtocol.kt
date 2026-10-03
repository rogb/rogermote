package com.rogbandroid.rogermote.tv.samsung

import java.util.Base64
import com.rogbandroid.rogermote.data.TvApplication
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

    fun installedApplicationsMessage(): String = emitMessage("ed.installedApp.get")

    fun launchApplicationMessage(applicationId: String): String = emitMessage(
        event = "ed.apps.launch",
        data = JSONObject()
            .put("action_type", "DEEP_LINK")
            .put("appId", applicationId)
            .put("metaTag", ""),
    )

    fun applicationIconMessage(iconPath: String): String = emitMessage(
        event = "ed.apps.icon",
        data = JSONObject().put("iconPath", iconPath),
    )

    private fun emitMessage(event: String, data: JSONObject? = null): String = JSONObject()
        .put("method", "ms.channel.emit")
        .put(
            "params",
            JSONObject()
                .put("event", event)
                .put("to", "host")
                .apply { data?.let { put("data", it) } },
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
            "ed.installedApp.get" -> SamsungEvent.Applications(parseApplications(root))
            "ed.apps.icon" -> SamsungEvent.ApplicationIcon(parseIconData(root))
            else -> SamsungEvent.Other
        }
    }.getOrDefault(SamsungEvent.Invalid)

    private fun parseApplications(root: JSONObject): List<TvApplication> {
        val data = root.optJSONObject("data") ?: return emptyList()
        val entries = data.optJSONArray("data") ?: return emptyList()
        return buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val id = entry.optString("appId").ifBlank { entry.optString("id") }
                val name = entry.optString("name")
                    .ifBlank { entry.optString("appName") }
                if (id.isNotBlank() && name.isNotBlank()) {
                    add(
                        TvApplication(
                            id = id,
                            name = name,
                            iconReference = entry.optString("icon")
                                .ifBlank { entry.optString("iconPath") }
                                .takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }
    }

    private fun parseIconData(root: JSONObject): String? {
        val data = root.opt("data")
        val objectData = data as? JSONObject
        return listOf(
            root.optString("icon"),
            root.optString("iconData"),
            root.optString("image"),
            root.optString("iconPath"),
            objectData?.optString("icon"),
            objectData?.optString("iconData"),
            objectData?.optString("image"),
            objectData?.optString("iconPath"),
            objectData?.optString("base64"),
            objectData?.optString("data"),
            data as? String,
        ).firstOrNull { !it.isNullOrBlank() }
    }
}

internal sealed interface SamsungEvent {
    data class Connected(val token: String?) : SamsungEvent
    data object Unauthorized : SamsungEvent
    data class Applications(val applications: List<TvApplication>) : SamsungEvent
    data class ApplicationIcon(val iconData: String?) : SamsungEvent
    data object Other : SamsungEvent
    data object Invalid : SamsungEvent
}
