package pk.vexel.healthpassport.core.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Password-based encryption for user-controlled backup files. The password is never persisted. */
object BackupCrypto {
    private val magic = "VEXEL-BACKUP-ENC-1".toByteArray(Charsets.US_ASCII)
    private const val iterations = 120_000
    private const val saltBytes = 16
    private const val ivBytes = 12
    private const val keyBits = 256
    private const val tagBits = 128

    fun isEncrypted(blob: ByteArray): Boolean = blob.size > magic.size && blob.copyOfRange(0, magic.size).contentEquals(magic)

    fun encrypt(plain: ByteArray, password: CharArray, random: SecureRandom = SecureRandom()): ByteArray {
        require(password.size >= 8) { "Backup password must contain at least 8 characters" }
        val salt = ByteArray(saltBytes).also(random::nextBytes)
        val iv = ByteArray(ivBytes).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key(password, salt), GCMParameterSpec(tagBits, iv))
        }
        password.fill('\u0000')
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(magic.size + 4 + salt.size + iv.size + encrypted.size)
            .put(magic).putInt(iterations).put(salt).put(iv).put(encrypted).array()
    }

    fun decrypt(blob: ByteArray, password: CharArray): ByteArray {
        require(blob.size > magic.size + 4 + saltBytes + ivBytes && blob.copyOfRange(0, magic.size).contentEquals(magic)) { "Unsupported or unencrypted backup format" }
        val buffer = ByteBuffer.wrap(blob).apply { position(magic.size) }
        val storedIterations = buffer.int
        require(storedIterations in 100_000..500_000) { "Unsupported backup security parameters" }
        val salt = ByteArray(saltBytes).also(buffer::get)
        val iv = ByteArray(ivBytes).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(password, salt, storedIterations), GCMParameterSpec(tagBits, iv))
            }
            cipher.doFinal(encrypted)
        } finally {
            password.fill('\u0000')
        }
    }

    private fun key(password: CharArray, salt: ByteArray, rounds: Int = iterations): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, rounds, keyBits)
        return try {
            SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
