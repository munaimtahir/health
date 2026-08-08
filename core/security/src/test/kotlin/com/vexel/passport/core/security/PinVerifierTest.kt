package com.vexel.passport.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinVerifierTest {
    @Test
    fun generated_record_matches_only_the_original_pin() {
        val verifier = PinVerifier()
        val record = verifier.create("2468".toCharArray())
        assertNotEquals("2468", record.digest.decodeToString())
        assertFalse(record.salt.contentEquals("2468".toByteArray()))
        assertTrue(verifier.matches("2468".toCharArray(), record))
        assertFalse(verifier.matches("2469".toCharArray(), record))
    }

    @Test(expected = IllegalArgumentException::class)
    fun short_pin_is_rejected() {
        PinVerifier().create("123".toCharArray())
    }
}
