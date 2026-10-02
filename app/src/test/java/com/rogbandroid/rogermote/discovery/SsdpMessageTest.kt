package com.rogbandroid.rogermote.discovery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SsdpMessageTest {
    @Test
    fun parsesHeadersWithoutDependingOnHeaderCase() {
        val message = SsdpMessageParser.parse(
            "NOTIFY * HTTP/1.1\r\n" +
                "LOCATION: http://192.168.1.50:9197/dmr\r\n" +
                "SERVER: SHP, UPnP/1.0, Samsung UPnP SDK/1.0\r\n" +
                "USN: uuid:test::upnp:rootdevice\r\n\r\n",
        )

        assertNotNull(message)
        assertEquals("http://192.168.1.50:9197/dmr", message?.headers?.get("location"))
        assertEquals("uuid:test::upnp:rootdevice", message?.headers?.get("usn"))
    }
}
