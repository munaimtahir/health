package pk.vexel.healthpassport.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinVerifierTest {
    @Test
    fun generated_record_matches_only_the_original_pin() {
        val verifier = PinVerifier()
        val record = verifier.create("2468".toCharArray())
        assertTrue(verifier.matches("2468".toCharArray(), record))
        assertFalse(verifier.matches("2469".toCharArray(), record))
    }

    @Test(expected = IllegalArgumentException::class)
    fun short_pin_is_rejected() {
        PinVerifier().create("123".toCharArray())
    }
}
