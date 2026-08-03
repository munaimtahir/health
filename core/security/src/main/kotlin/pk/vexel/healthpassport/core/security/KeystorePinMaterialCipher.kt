package pk.vexel.healthpassport.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface PinMaterialCipher {
    fun encrypt(record: PinRecord): String
    fun decrypt(value: String): PinRecord
}

class KeystorePinMaterialCipher : PinMaterialCipher {
    override fun encrypt(record: PinRecord): String {
        val plain = ByteBuffer.allocate(8 + record.salt.size + record.digest.size).putInt(record.iterations).putInt(record.salt.size).put(record.salt).put(record.digest).array()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        return Base64.encodeToString(cipher.iv + cipher.doFinal(plain), Base64.NO_WRAP)
    }

    override fun decrypt(value: String): PinRecord {
        val blob = Base64.decode(value, Base64.NO_WRAP)
        val iv = blob.copyOfRange(0, IV_BYTES)
        val plain = Cipher.getInstance(TRANSFORMATION).run { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv)); doFinal(blob.copyOfRange(IV_BYTES, blob.size)) }
        val buffer = ByteBuffer.wrap(plain)
        val iterations = buffer.int
        val salt = ByteArray(buffer.int).also(buffer::get)
        val digest = ByteArray(buffer.remaining()).also(buffer::get)
        return PinRecord(salt, digest, iterations)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!store.containsAlias(KEY_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
                init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
                generateKey()
            }
        }
        return store.getKey(KEY_ALIAS, null) as SecretKey
    }

    private companion object {
        const val KEY_ALIAS = "vexel_pin_material"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
