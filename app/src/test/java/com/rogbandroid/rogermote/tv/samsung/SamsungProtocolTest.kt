package com.rogbandroid.rogermote.tv.samsung

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SamsungProtocolTest {
    @Test
    fun remoteUrlEncodesAppNameAndOptionalToken() {
        val withoutToken = SamsungProtocol.remoteUrl("192.168.1.50", "Rogermote", null)
        val withToken = SamsungProtocol.remoteUrl("192.168.1.50", "Rogermote", "12345678")

        assertTrue(withoutToken.startsWith("https://192.168.1.50:8002/"))
        assertTrue(withoutToken.contains("name=Um9nZXJtb3Rl"))
        assertFalse(withoutToken.contains("token="))
        assertTrue(withToken.contains("token=12345678"))
    }

    @Test
    fun volumeUpUsesSamsungRemoteControlPayload() {
        val message = JSONObject(SamsungProtocol.commandMessage(SamsungKey.VolumeUp))
        val params = message.getJSONObject("params")

        assertEquals("ms.remote.control", message.getString("method"))
        assertEquals("Click", params.getString("Cmd"))
        assertEquals("KEY_VOLUP", params.getString("DataOfCmd"))
        assertEquals("false", params.getString("Option"))
        assertEquals("SendRemoteKey", params.getString("TypeOfRemote"))
    }

    @Test
    fun connectionEventReturnsPairingToken() {
        val event = SamsungProtocol.parseEvent(
            """{"event":"ms.channel.connect","data":{"token":"12345678"}}""",
        )

        assertEquals(SamsungEvent.Connected("12345678"), event)
    }

    @Test
    fun unauthorizedEventIsRecognized() {
        val event = SamsungProtocol.parseEvent("""{"event":"ms.channel.unauthorized"}""")

        assertEquals(SamsungEvent.Unauthorized, event)
    }

    @Test
    fun installedApplicationRequestUsesSamsungEmitEvent() {
        val message = JSONObject(SamsungProtocol.installedApplicationsMessage())
        val params = message.getJSONObject("params")

        assertEquals("ms.channel.emit", message.getString("method"))
        assertEquals("ed.installedApp.get", params.getString("event"))
        assertEquals("host", params.getString("to"))
        assertFalse(params.has("data"))
    }

    @Test
    fun installedApplicationsResponseReturnsDiscoveredMetadata() {
        val event = SamsungProtocol.parseEvent(
            """{"event":"ed.installedApp.get","data":{"data":[{"appId":"youtube.id","name":"YouTube","icon":"/icons/youtube.png"}]}}""",
        )

        assertEquals(
            SamsungEvent.Applications(
                listOf(
                    com.rogbandroid.rogermote.data.TvApplication(
                        id = "youtube.id",
                        name = "YouTube",
                        iconReference = "/icons/youtube.png",
                    ),
                ),
            ),
            event,
        )
    }

    @Test
    fun launchApplicationUsesDiscoveredId() {
        val message = JSONObject(SamsungProtocol.launchApplicationMessage("youtube.id"))
        val params = message.getJSONObject("params")

        assertEquals("ed.apps.launch", params.getString("event"))
        val data = params.getJSONObject("data")
        assertEquals("youtube.id", data.getString("appId"))
        assertEquals("DEEP_LINK", data.getString("action_type"))
        assertEquals("", data.getString("metaTag"))
    }

    @Test
    fun applicationIconRequestUsesReturnedIconPath() {
        val message = JSONObject(SamsungProtocol.applicationIconMessage("/icons/youtube.png"))
        val params = message.getJSONObject("params")

        assertEquals("ed.apps.icon", params.getString("event"))
        assertEquals("/icons/youtube.png", params.getJSONObject("data").getString("iconPath"))
    }
}
