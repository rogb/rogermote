package com.rogbandroid.rogermote.discovery

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Xml
import com.rogbandroid.rogermote.data.TvDevice
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SamsungSsdpDiscovery(context: Context) {
    private val wifiManager = context.applicationContext
        .getSystemService(WifiManager::class.java)

    suspend fun scan(timeoutMillis: Long = DEFAULT_SCAN_TIMEOUT_MILLIS): List<TvDevice> =
        withContext(Dispatchers.IO) {
            val networkInterface = findMulticastInterface() ?: return@withContext emptyList()
            val lock = wifiManager?.createMulticastLock("RogermoteSsdpDiscovery")?.apply {
                setReferenceCounted(false)
                acquire()
            }
            try {
                scanOnInterface(networkInterface, timeoutMillis)
            } finally {
                lock?.release()
            }
        }

    private fun scanOnInterface(
        networkInterface: NetworkInterface,
        timeoutMillis: Long,
    ): List<TvDevice> {
        val group = InetAddress.getByName(SSDP_MULTICAST_ADDRESS)
        val devices = linkedMapOf<String, TvDevice>()
        MulticastSocket(SSDP_PORT).use { socket ->
            socket.reuseAddress = true
            socket.networkInterface = networkInterface
            socket.joinGroup(InetSocketAddress(group, SSDP_PORT), networkInterface)
            try {
                sendSearch(socket, group)
                val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
                val buffer = ByteArray(MAX_PACKET_SIZE)
                while (System.nanoTime() < deadline) {
                    val remainingMillis = ((deadline - System.nanoTime()) /
                        TimeUnit.MILLISECONDS.toNanos(1)).coerceAtLeast(1L)
                    socket.soTimeout = remainingMillis.coerceAtMost(SOCKET_TIMEOUT_MILLIS).toInt()
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket.receive(packet)
                    } catch (_: java.net.SocketTimeoutException) {
                        continue
                    }
                    val message = SsdpMessageParser.parse(
                        String(packet.data, packet.offset, packet.length, Charsets.UTF_8),
                    ) ?: continue
                    if (!message.isSamsungAdvertisement()) continue

                    val ipAddress = packet.address.hostAddress ?: continue
                    val device = deviceFrom(message, ipAddress) ?: continue
                    devices[ipAddress] = device
                }
            } finally {
                socket.leaveGroup(InetSocketAddress(group, SSDP_PORT), networkInterface)
            }
        }
        return devices.values.toList()
    }

    private fun sendSearch(socket: MulticastSocket, group: InetAddress) {
        val request = buildString {
            append("M-SEARCH * HTTP/1.1\r\n")
            append("HOST: $SSDP_MULTICAST_ADDRESS:$SSDP_PORT\r\n")
            append("MAN: \"ssdp:discover\"\r\n")
            append("MX: 2\r\n")
            append("ST: ssdp:all\r\n")
            append("\r\n")
        }.toByteArray(Charsets.UTF_8)
        socket.send(DatagramPacket(request, request.size, group, SSDP_PORT))
    }

    private fun deviceFrom(message: SsdpMessage, ipAddress: String): TvDevice? {
        val location = message.headers["location"]
        val description = location?.let(::readDescription)
        val friendlyName = description?.friendlyName
            ?: message.headers["friendlyname"]
            ?: "Samsung TV"
        return TvDevice(
            ipAddress = ipAddress,
            friendlyName = friendlyName,
            modelName = description?.modelName,
            uniqueId = description?.uniqueId ?: message.headers["usn"],
        )
    }

    private fun readDescription(location: String): DeviceDescription? = runCatching {
        val connection = URL(location).openConnection().apply {
            connectTimeout = DESCRIPTION_TIMEOUT_MILLIS.toInt()
            readTimeout = DESCRIPTION_TIMEOUT_MILLIS.toInt()
            useCaches = false
        }
        connection.getInputStream().use { input ->
            val parser = Xml.newPullParser().apply {
                setInput(input, Charsets.UTF_8.name())
            }
            var friendlyName: String? = null
            var modelName: String? = null
            var uniqueId: String? = null
            var event = parser.eventType
            while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (event == org.xmlpull.v1.XmlPullParser.START_TAG) {
                    when (parser.name.lowercase()) {
                        "friendlyname" -> friendlyName = parser.nextText().trim()
                        "modelname" -> modelName = parser.nextText().trim()
                        "udn", "deviceid" -> uniqueId = parser.nextText().trim()
                    }
                }
                event = parser.next()
            }
            DeviceDescription(friendlyName, modelName, uniqueId)
        }
    }.getOrNull()

    private fun findMulticastInterface(): NetworkInterface? =
        NetworkInterface.getNetworkInterfaces().toList().firstOrNull { networkInterface ->
            runCatching {
                networkInterface.isUp &&
                    !networkInterface.isLoopback &&
                    networkInterface.supportsMulticast() &&
                    networkInterface.inetAddresses.toList().any { !it.isLoopbackAddress }
            }.getOrDefault(false)
        }

    private companion object {
        const val SSDP_MULTICAST_ADDRESS = "239.255.255.250"
        const val SSDP_PORT = 1900
        const val SOCKET_TIMEOUT_MILLIS = 250L
        const val DESCRIPTION_TIMEOUT_MILLIS = 600L
        const val DEFAULT_SCAN_TIMEOUT_MILLIS = 3_500L
        const val MAX_PACKET_SIZE = 8 * 1024
    }
}

private data class DeviceDescription(
    val friendlyName: String?,
    val modelName: String?,
    val uniqueId: String?,
)

private fun SsdpMessage.isSamsungAdvertisement(): Boolean {
    val searchableText = listOf(
        headers["server"],
        headers["location"],
        headers["st"],
        headers["usn"],
    ).filterNotNull().joinToString(" ")
    return searchableText.contains("samsung", ignoreCase = true) ||
        headers["location"]?.contains(":9197/", ignoreCase = true) == true
}
