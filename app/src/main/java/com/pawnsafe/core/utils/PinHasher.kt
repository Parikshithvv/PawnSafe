package com.pawnsafe.core.utils

import java.security.MessageDigest

object PinHasher {
    fun hash(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, storedHash: String): Boolean = hash(pin) == storedHash
}