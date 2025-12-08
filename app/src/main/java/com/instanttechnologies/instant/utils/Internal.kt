package com.instanttechnologies.instant.utils

import android.content.Context
import android.widget.Toast
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