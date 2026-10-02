package com.rogbandroid.rogermote.tv.samsung

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
class SamsungTokenStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val ipAddress = "192.0.2.10"
    private val token = "test-pairing-token"

    @Before
    fun clearBeforeTest() {
        KeystoreSamsungTokenStore(context).clear(ipAddress)
    }

    @After
    fun clearAfterTest() {
        KeystoreSamsungTokenStore(context).clear(ipAddress)
    }

    @Test
    fun tokenSurvivesStoreRecreationAndIsNotStoredAsPlaintext() {
        KeystoreSamsungTokenStore(context).write(ipAddress, token)

        val recreatedStore = KeystoreSamsungTokenStore(context)
        assertEquals(token, recreatedStore.read(ipAddress))

        val storedValues = context
            .getSharedPreferences("samsung_pairing", Context.MODE_PRIVATE)
            .all
            .values
            .filterIsInstance<String>()
        assertFalse(storedValues.any { it.contains(token) })

        recreatedStore.clear(ipAddress)
        assertNull(recreatedStore.read(ipAddress))
    }
}
