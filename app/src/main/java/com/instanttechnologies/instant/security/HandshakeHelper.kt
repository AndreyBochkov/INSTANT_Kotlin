package com.instanttechnologies.instant.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.crypto.tink.subtle.Hkdf
import com.google.crypto.tink.subtle.X25519
import com.instanttechnologies.instant.utils.extractRawPublicKeyFromX509
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

class HandshakeHelper {

    private val keyAlias = "instant_identity_key"
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    private val privateKey = X25519.generatePrivateKey()
    var handshakeBytes = byteArrayOf()
    private var sharedMaster = byteArrayOf()
    var requestID = ""

    init {
        val publicKey = X25519.publicFromPrivate(privateKey)
        val identityKeyPair = getOrCreateIdentityKeyPair()
        handshakeBytes = byteArrayOf(0x2) + // INSTANT PROTOCOL VERSION
                extractRawPublicKeyFromX509(identityKeyPair.public.encoded) +
                publicKey +
                signWithIdentityKey(identityKeyPair.private, publicKey)
    }

    fun finishHandshake(serverHandshakeBytes: ByteArray) {
        val sharedPre = X25519.computeSharedSecret(privateKey, serverHandshakeBytes.sliceArray(0..31))
        sharedMaster = Hkdf.computeHkdf(
            "HMACSHA256",
            sharedPre,
            serverHandshakeBytes.sliceArray(32..63),
            byteArrayOf(),
            32
        )
        requestID = serverHandshakeBytes.sliceArray(64..serverHandshakeBytes.lastIndex).decodeToString()
    }

    fun rotateKey(messageBytes: ByteArray) {
        sharedMaster = Hkdf.computeHkdf(
            "HMACSHA256",
            messageBytes,
            sharedMaster,
            byteArrayOf(),
            32
        )
    }

    fun <T>encrypt(req: T, serializer: KSerializer<T>): ByteArray =
        INSTAESHelper.encrypt(sharedMaster, Json.encodeToString(serializer, req).toByteArray())

    fun decrypt(req: ByteArray): ByteArray =
        INSTAESHelper.decrypt(sharedMaster, req)

    fun changeIdentityKey(): ByteArray {
        return extractRawPublicKeyFromX509(replaceIdentityKeyPair().public.encoded)
    }

    private fun signWithIdentityKey(privateKey: PrivateKey, data: ByteArray): ByteArray {
        return Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
            update(data)
        }.sign()
    }

    private fun getOrCreateIdentityKeyPair(): KeyPair {
        return if (keyStore.containsAlias(keyAlias)) {
            val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
            val publicKey = keyStore.getCertificate(keyAlias).publicKey
            KeyPair(publicKey, privateKey)
        } else {
            replaceIdentityKeyPair()
        }
    }

    private fun replaceIdentityKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)
            .build()

        keyPairGenerator.initialize(keySpec)
        return keyPairGenerator.generateKeyPair()
    }
}