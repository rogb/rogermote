package com.rogbandroid.rogermote.data

data class TvDevice(
    val ipAddress: String,
    val friendlyName: String,
    val modelName: String? = null,
    val uniqueId: String? = null,
)
