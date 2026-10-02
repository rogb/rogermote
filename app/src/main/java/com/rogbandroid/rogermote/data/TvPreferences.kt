package com.rogbandroid.rogermote.data

import android.content.Context

internal class TvPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun readIpAddress(): String? = preferences.getString(KEY_IP_ADDRESS, null)
        ?.trim()
        ?.takeIf(String::isNotEmpty)

    fun writeIpAddress(ipAddress: String) {
        preferences.edit().putString(KEY_IP_ADDRESS, ipAddress).apply()
    }

    fun readHapticsEnabled(): Boolean = preferences.getBoolean(KEY_HAPTICS_ENABLED, true)

    fun writeHapticsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_IP_ADDRESS).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "tv_configuration"
        const val KEY_IP_ADDRESS = "ip_address"
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    }
}
