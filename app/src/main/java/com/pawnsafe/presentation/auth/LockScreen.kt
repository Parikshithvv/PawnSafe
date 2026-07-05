package com.pawnsafe.presentation.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context     = LocalContext.current
    val authState   by viewModel.authState.collectAsState()
    val bioEnabled  by viewModel.bioEnabled.collectAsState()
    val failCount   by viewModel.failCount.collectAsState()
    val lockedUntil by viewModel.lockedUntil.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    val isLocked   = lockedUntil > System.currentTimeMillis()
    val triesLeft  = 10 - failCount

    LaunchedEffect(authState) {
        if (authState is AuthState.Unlocked) onUnlocked()
    }

    // Auto-launch biometric on open
    LaunchedEffect(bioEnabled) {
        if (bioEnabled) {
            kotlinx.coroutines.delay(500)
            launchBiometric(context, viewModel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("Enter PIN", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val errorMsg = when {
            isLocked      -> "Too many attempts — locked for 5 minutes"
            failCount > 0 -> "$triesLeft attempt${if (triesLeft == 1) "" else "s"} remaining"
            else          -> null
        }
        if (errorMsg != null) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < enteredPin.length) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        val keys = listOf(
            listOf("1","2","3"),
            listOf("4","5","6"),
            listOf("7","8","9"),
            listOf("bio","0","del")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "del" -> {
                            IconButton(
                                onClick = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1) },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(28.dp))
                            }
                        }
                        "bio" -> {
                            if (bioEnabled) {
                                IconButton(
                                    onClick = { launchBiometric(context, viewModel) },
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        contentDescription = "Biometric",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Spacer(Modifier.size(72.dp))
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = {
                                    if (!isLocked && enteredPin.length < 4) {
                                        enteredPin += key
                                        if (enteredPin.length == 4) {
                                            viewModel.verifyPin(enteredPin)
                                            enteredPin = ""
                                        }
                                    }
                                },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun launchBiometric(context: android.content.Context, viewModel: AuthViewModel) {
    val activity = context as? FragmentActivity ?: return
    val biometricManager = BiometricManager.from(context)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        != BiometricManager.BIOMETRIC_SUCCESS) return
    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            viewModel.onBiometricSuccess()
        }
    })
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Unlock")
            .setSubtitle("Use fingerprint to unlock PawnSafe")
            .setNegativeButtonText("Use PIN")
            .build()
    )
}