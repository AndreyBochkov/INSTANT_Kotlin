package com.instanttechnologies.instant.security

import android.content.Context
import com.instanttechnologies.instant.R
import java.io.File
import java.io.FileOutputStream

class EncryptedStorage(context: Context) {
    private val keyStoreHelper = AESKeyStoreHelper("instant_encrypted_storage_key")

    private val addressFile = File(context.filesDir, "encrypted_address")
    private val dateTimeFile = File(context.filesDir, "datetime")
    private val dateTimeDefaults = context.getString(R.string.dateDefault) to context.getString(R.string.timeDefault)

    private val chatsFile = File(context.filesDir, "encrypted_chats")
    private val loginFile = File(context.filesDir, "encrypted_login")
    init {
        try {
            chatsFile.delete()
        } catch (_: Throwable) {
//            Silently exit if cannot delete
        }
        try {
            loginFile.delete()
        } catch (_: Throwable) {
//            Silently exit if cannot delete
        }
    }

    fun saveAddress(address: String) = saveString(addressFile, address)
    fun loadAddress(): String? = loadString(addressFile)

    fun saveDateTime(date: String, time: String) {
        FileOutputStream(dateTimeFile).use { outputStream ->
            outputStream.write(date.length)
            outputStream.write(date.toByteArray())
            outputStream.write(time.length)
            outputStream.write(time.toByteArray())
        }
    }

    // работает - не трогаем
    fun loadDateTime(): Pair<String, String> {
        if (!dateTimeFile.exists()) return dateTimeDefaults
        return try {
            val bytes = dateTimeFile.readBytes()
            bytes.copyOfRange(1, 1+bytes[0]).decodeToString() to
                    bytes.copyOfRange(2+bytes[0], 2+bytes[0]+bytes[1+bytes[0]]).decodeToString()
        } catch (_: Exception) {
            dateTimeDefaults
        }
    }

//    fun savePassword(password: String) = saveString(passwordFile, password)
//    fun loadPassword(): String = loadString(passwordFile)

    private fun saveString(file: File, value: String) {
        val (encrypted, iv) = keyStoreHelper.encrypt(value.toByteArray())
        FileOutputStream(file).use { outputStream ->
            outputStream.write(iv)
            outputStream.write(encrypted)
        }
    }

    private fun loadString(file: File): String? {
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            val iv = bytes.copyOfRange(0, 12)
            val encrypted = bytes.copyOfRange(12, bytes.size)
            keyStoreHelper.decrypt(encrypted, iv).toString(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
//
//    fun saveChats(chats: List<Chat>) {
//        val json = Json.encodeToString(chats)
//        val (encrypted, iv) = keyStoreHelper.encrypt(json.toByteArray())
//
//        FileOutputStream(chatsFile).use { outputStream ->
//            outputStream.write(encrypted.size)
//            outputStream.write(encrypted)
//            outputStream.write(iv)
//        }
//    }
//
//    fun loadChats(): List<Chat> {
//        if (!chatsFile.exists()) return emptyList()
//
//        return try {
//            val bytes = chatsFile.readBytes()
//            val input = ByteArrayInputStream(bytes)
//
//            val encryptedSize = input.read()
//            val encrypted = ByteArray(encryptedSize).also {
//                input.read(it)
//            }
//            val iv = ByteArray(12).also {
//                input.read(it)
//            }
//
//            val decrypted = keyStoreHelper.decrypt(encrypted, iv)
//            Json.decodeFromString<List<Chat>>(decrypted.decodeToString())
//        } catch (_: Exception) {
//            emptyList()
//        }
//    }
}