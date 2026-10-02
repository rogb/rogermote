package com.rogbandroid.rogermote.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TvPreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var preferences: TvPreferences

    @Before
    fun setUp() {
        preferences = TvPreferences(context)
        preferences.clear()
    }

    @After
    fun tearDown() {
        preferences.clear()
    }

    @Test
    fun ipAddressSurvivesStoreRecreationAndCanBeCleared() {
        preferences.writeIpAddress(" 192.168.1.50 ")

        assertEquals("192.168.1.50", TvPreferences(context).readIpAddress())

        preferences.clear()
        assertNull(TvPreferences(context).readIpAddress())
    }

    @Test
    fun clearingTvDoesNotResetHapticsPreference() {
        preferences.writeHapticsEnabled(false)
        preferences.writeIpAddress("192.168.1.50")

        preferences.clear()

        assertFalse(TvPreferences(context).readHapticsEnabled())
    }
}
