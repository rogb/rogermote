package com.rogbandroid.rogermote.data

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DiscoveredTvPersistenceTest {
    private val context = object : ContextWrapper(ApplicationProvider.getApplicationContext<Context>()) {
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            super.getSharedPreferences("discovered_tv_test", mode)
    }

    @After
    fun cleanup() {
        context.getSharedPreferences("discovered_tv_test", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun listAndDeletionSurviveStoreRecreationWithoutChangingSelectedTv() {
        val first = TvDevice("192.168.1.10", "Living room", "Q80R", "uuid:first", "Family Room TV")
        val second = TvDevice("192.168.1.11", "Bedroom")
        val store = TvPreferences(context)
        store.writeIpAddress(first.ipAddress)
        store.writeDiscoveredDevices(listOf(first, second))
        assertEquals(listOf(first, second), TvPreferences(context).readDiscoveredDevices())

        store.writeDiscoveredDevices(listOf(second))
        assertEquals(listOf(second), TvPreferences(context).readDiscoveredDevices())
        assertEquals(first.ipAddress, TvPreferences(context).readIpAddress())

        store.clear()
        assertEquals(listOf(second), TvPreferences(context).readDiscoveredDevices())
    }
}
