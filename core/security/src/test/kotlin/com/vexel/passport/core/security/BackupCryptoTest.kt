package com.vexel.passport.core.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BackupCryptoTest {
    @Test fun encrypted_backup_round_trips_without_plaintext() {
        val plain = "synthetic health backup".toByteArray()
        val encrypted = BackupCrypto.encrypt(plain, "correct horse battery".toCharArray())
        assertNotEquals(String(plain), String(encrypted))
        assertArrayEquals(plain, BackupCrypto.decrypt(encrypted, "correct horse battery".toCharArray()))
    }

    @Test(expected = Exception::class)
    fun wrong_password_is_rejected() {
        val encrypted = BackupCrypto.encrypt("synthetic".toByteArray(), "correct horse battery".toCharArray())
        BackupCrypto.decrypt(encrypted, "wrong password".toCharArray())
    }

    @Test(expected = Exception::class)
    fun tampered_ciphertext_is_rejected() {
        val encrypted = BackupCrypto.encrypt("synthetic".toByteArray(), "correct horse battery".toCharArray())
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 0x01).toByte()
        BackupCrypto.decrypt(encrypted, "correct horse battery".toCharArray())
    }
}
