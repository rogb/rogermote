package com.rogbandroid.rogermote.discovery

internal data class SsdpMessage(
    val startLine: String,
    val headers: Map<String, String>,
)

internal object SsdpMessageParser {
    fun parse(message: String): SsdpMessage? {
        val lines = message.split("\r\n", "\n")
        val startLine = lines.firstOrNull()?.trim().orEmpty()
        if (startLine.isEmpty()) return null

        val headers = buildMap {
            lines.drop(1).forEach { line ->
                val separator = line.indexOf(':')
                if (separator <= 0) return@forEach
                put(
                    line.substring(0, separator).trim().lowercase(),
                    line.substring(separator + 1).trim(),
                )
            }
        }
        return SsdpMessage(startLine, headers)
    }
}
