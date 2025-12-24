package com.instanttechnologies.instant.utils

import android.content.Context
import android.widget.Toast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun extractRawPublicKeyFromX509(x509Encoded: ByteArray): ByteArray {
    return x509Encoded.copyOfRange(x509Encoded.size - 64, x509Encoded.size)
}

object DateTimeConverter {
    var dateTime = "" to ""

    fun actualizeDateTime(new: Pair<String, String>) {
        dateTime = new
    }

    fun unixToHMSString(pattern: String = dateTime.second, ts: Long): String {
        if (pattern.isEmpty()) {
            return "Empty time pattern!"
        }
        return try {
                SimpleDateFormat(pattern, Locale.getDefault()).format(Date(ts*1000))
            } catch (e: IllegalArgumentException) {
                pattern
            }
    }

    fun unixToYMDString(pattern: String = dateTime.first, ts: Long): String {
        if (pattern.isEmpty()) {
            return "Empty date pattern!"
        }
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(ts*1000))
        } catch (e: IllegalArgumentException) {
            pattern
        }
    }
}

object ByteArrayAsUnsignedListSerializer : KSerializer<ByteArray> {
    override val descriptor = ListSerializer(Int.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: ByteArray) {
        val unsignedList = value.map { it.toInt() and 0xFF }
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), unsignedList)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val unsignedList = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return unsignedList.map { it.toByte() }.toByteArray()
    }
}