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
}
