package pk.vexel.healthpassport.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val DEFAULT_ITERATIONS = 120_000

data class PinRecord(val salt: ByteArray, val digest: ByteArray, val iterations: Int = DEFAULT_ITERATIONS)

class PinVerifier(private val random: SecureRandom = SecureRandom()) {
    fun create(pin: CharArray): PinRecord {
        require(pin.size in 4..12) { "PIN must contain 4 to 12 digits" }
        require(pin.all(Char::isDigit)) { "PIN must contain digits only" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        return PinRecord(salt, derive(pin, salt, DEFAULT_ITERATIONS))
    }

    fun matches(pin: CharArray, record: PinRecord): Boolean {
        if (pin.size !in 4..12 || !pin.all(Char::isDigit)) return false
        return MessageDigest.isEqual(record.digest, derive(pin, record.salt, record.iterations))
    }

    private fun derive(pin: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, DIGEST_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            pin.fill('\u0000')
        }
    }

    private companion object {
        const val SALT_BYTES = 16
        const val DIGEST_BITS = 256
    }
}
