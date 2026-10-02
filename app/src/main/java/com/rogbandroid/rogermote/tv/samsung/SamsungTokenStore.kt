package com.rogbandroid.rogermote.tv.samsung

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal interface SamsungTokenStore {
    fun read(ipAddress: String): String?
    fun write(ipAddress: String, token: String)
    fun clear(ipAddress: String)
}

internal class KeystoreSamsungTokenStore(context: Context) : SamsungTokenStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(ipAddress: String): String? {
        val storedValue = preferences.getString(preferenceKey(ipAddress), null) ?: return null
        return runCatching {
            val (ivText, cipherText) = storedValue.split(':', limit = 2)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                encryptionKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.getDecoder().decode(ivText)),
            )
            String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), Charsets.UTF_8)
        }.getOrElse {
            clear(ipAddress)
            null
        }
    }

    override fun write(ipAddress: String, token: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val storedValue = buildString {
            append(Base64.getEncoder().encodeToString(cipher.iv))
            append(':')
            append(Base64.getEncoder().encodeToString(cipher.doFinal(token.toByteArray(Charsets.UTF_8))))
        }
        preferences.edit().putString(preferenceKey(ipAddress), storedValue).apply()
    }

    override fun clear(ipAddress: String) {
        preferences.edit().remove(preferenceKey(ipAddress)).apply()
    }

    private fun encryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }

    private fun preferenceKey(ipAddress: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(ipAddress.toByteArray(Charsets.UTF_8))
        return "token_${Base64.getUrlEncoder().withoutPadding().encodeToString(digest)}"
    }

    private companion object {
        const val PREFERENCES_NAME = "samsung_pairing"
        const val KEY_ALIAS = "rogermote_samsung_pairing_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
