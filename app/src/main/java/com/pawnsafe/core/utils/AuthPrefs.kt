package com.pawnsafe.core.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

object AuthPrefsKeys {
    val PIN_HASH        = stringPreferencesKey("pin_hash")
    val PIN_ENABLED     = booleanPreferencesKey("pin_enabled")
    val BIO_ENABLED     = booleanPreferencesKey("bio_enabled")
    val FAIL_COUNT      = intPreferencesKey("fail_count")
    val LOCKED_UNTIL    = longPreferencesKey("locked_until")
}

class AuthPrefs(private val context: Context) {

    val pinEnabled: Flow<Boolean> = context.authDataStore.data
        .map { it[AuthPrefsKeys.PIN_ENABLED] ?: false }

    val bioEnabled: Flow<Boolean> = context.authDataStore.data
        .map { it[AuthPrefsKeys.BIO_ENABLED] ?: false }

    val pinHash: Flow<String?> = context.authDataStore.data
        .map { it[AuthPrefsKeys.PIN_HASH] }

    val failCount: Flow<Int> = context.authDataStore.data
        .map { it[AuthPrefsKeys.FAIL_COUNT] ?: 0 }

    val lockedUntil: Flow<Long> = context.authDataStore.data
        .map { it[AuthPrefsKeys.LOCKED_UNTIL] ?: 0L }

    suspend fun setPin(hash: String) {
        context.authDataStore.edit {
            it[AuthPrefsKeys.PIN_HASH]    = hash
            it[AuthPrefsKeys.PIN_ENABLED] = true
            it[AuthPrefsKeys.FAIL_COUNT]  = 0
            it[AuthPrefsKeys.LOCKED_UNTIL] = 0L
        }
    }

    suspend fun clearPin() {
        context.authDataStore.edit {
            it.remove(AuthPrefsKeys.PIN_HASH)
            it[AuthPrefsKeys.PIN_ENABLED] = false
            it[AuthPrefsKeys.BIO_ENABLED] = false
            it[AuthPrefsKeys.FAIL_COUNT]  = 0
            it[AuthPrefsKeys.LOCKED_UNTIL] = 0L
        }
    }

    suspend fun setBioEnabled(enabled: Boolean) {
        context.authDataStore.edit { it[AuthPrefsKeys.BIO_ENABLED] = enabled }
    }

    suspend fun recordFailure() {
        context.authDataStore.edit {
            val count = (it[AuthPrefsKeys.FAIL_COUNT] ?: 0) + 1
            it[AuthPrefsKeys.FAIL_COUNT] = count
            if (count >= 10) {
                it[AuthPrefsKeys.LOCKED_UNTIL] = System.currentTimeMillis() + 5 * 60 * 1000L
            }
        }
    }

    suspend fun resetFailures() {
        context.authDataStore.edit {
            it[AuthPrefsKeys.FAIL_COUNT]   = 0
            it[AuthPrefsKeys.LOCKED_UNTIL] = 0L
        }
    }
}