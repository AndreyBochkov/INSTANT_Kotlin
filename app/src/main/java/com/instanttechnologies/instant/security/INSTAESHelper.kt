package com.instanttechnologies.instant.security

import com.google.crypto.tink.subtle.AesGcmJce

object INSTAESHelper {
    private const val IV_LENGTH = 12

    fun encrypt(key: ByteArray, plaintext: ByteArray): ByteArray {
        val ciphertext = AesGcmJce(key).encrypt(plaintext, null)
        return ciphertext
    }

    fun decrypt(key: ByteArray, encrypted: ByteArray): ByteArray {
        require(encrypted.size > IV_LENGTH) { "Invalid encrypted data" }
        return AesGcmJce(key).decrypt(encrypted, null)
    }
}