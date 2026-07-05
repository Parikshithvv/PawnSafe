package com.pawnsafe.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.core.utils.AuthPrefs
import com.pawnsafe.core.utils.PinHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle       : AuthState()
    object Unlocked   : AuthState()
    object WrongPin   : AuthState()
    object Locked     : AuthState()
    data class SetupDone(val pin: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = AuthPrefs(application)

    val pinEnabled  = prefs.pinEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val bioEnabled  = prefs.bioEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val failCount   = prefs.failCount.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val lockedUntil = prefs.lockedUntil.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun verifyPin(entered: String) {
        viewModelScope.launch {
            val locked = prefs.lockedUntil.first()
            if (locked > System.currentTimeMillis()) {
                _authState.value = AuthState.Locked
                return@launch
            }
            val storedHash = prefs.pinHash.first()
            if (storedHash != null && PinHasher.verify(entered, storedHash)) {
                prefs.resetFailures()
                _authState.value = AuthState.Unlocked
            } else {
                prefs.recordFailure()
                val count = prefs.failCount.first()
                if (count >= 10) _authState.value = AuthState.Locked
                else _authState.value = AuthState.WrongPin
            }
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            prefs.setPin(PinHasher.hash(pin))
            _authState.value = AuthState.SetupDone(pin)
        }
    }

    fun disablePin() {
        viewModelScope.launch { prefs.clearPin() }
    }

    fun setBioEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setBioEnabled(enabled) }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            prefs.resetFailures()
            _authState.value = AuthState.Unlocked
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}