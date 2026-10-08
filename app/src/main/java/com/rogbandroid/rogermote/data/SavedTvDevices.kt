package com.rogbandroid.rogermote.data

import org.json.JSONArray
import org.json.JSONObject

internal object SavedTvDevices {
    fun encode(devices: List<TvDevice>): String = JSONArray().apply {
        devices.forEach { device ->
            put(JSONObject().apply {
                put("ipAddress", device.ipAddress)
                put("friendlyName", device.friendlyName)
                put("modelName", device.modelName)
                put("uniqueId", device.uniqueId)
                put("alias", device.alias)
            })
        }
    }.toString()

    fun decode(json: String?): List<TvDevice> = runCatching {
        val entries = JSONArray(json ?: "[]")
        buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val ip = entry.optString("ipAddress").takeIf { it.isNotBlank() } ?: continue
                add(TvDevice(
                    ipAddress = ip,
                    friendlyName = entry.optString("friendlyName").ifBlank { "Samsung TV" },
                    modelName = entry.optionalString("modelName"),
                    uniqueId = entry.optionalString("uniqueId"),
                    alias = entry.optionalString("alias").orEmpty(),
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun merge(saved: List<TvDevice>, found: List<TvDevice>): List<TvDevice> {
        val result = saved.toMutableList()
        for (device in found) {
            val index = result.indexOfFirst {
                it.ipAddress == device.ipAddress ||
                    (!device.uniqueId.isNullOrBlank() && it.uniqueId == device.uniqueId)
            }
            if (index < 0) {
                result.add(device)
            } else {
                val previous = result[index]
                result[index] = device.copy(
                    modelName = device.modelName?.takeIf { it.isNotBlank() } ?: previous.modelName,
                    uniqueId = device.uniqueId?.takeIf { it.isNotBlank() } ?: previous.uniqueId,
                    alias = previous.alias,
                )
            }
        }
        return result
    }

    private fun JSONObject.optionalString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
