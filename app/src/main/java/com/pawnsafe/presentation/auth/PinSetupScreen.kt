package com.pawnsafe.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onBack: () -> Unit,
    onSetupComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    var firstPin   by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var phase      by remember { mutableStateOf(1) }
    var mismatch   by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.SetupDone) {
            viewModel.resetState()
            onSetupComplete()
        }
    }

    val isConfirmStep = phase == 2
    val displayPin    = if (isConfirmStep) confirmPin else firstPin

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isConfirmStep) "Confirm PIN" else "Set PIN") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isConfirmStep) "Re-enter your PIN" else "Choose a 4-digit PIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (isConfirmStep) "Enter the same PIN to confirm" else "You'll use this to unlock the app",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (mismatch) {
                Spacer(Modifier.height(8.dp))
                Text("PINs don't match. Try again", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < displayPin.length) MaterialTheme.colorScheme.primary
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
                listOf("","0","del")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { key ->
                        when (key) {
                            "del" -> {
                                IconButton(onClick = {
                                    mismatch = false
                                    if (phase == 1) { if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1) }
                                    else { if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1) }
                                }, modifier = Modifier.size(72.dp)) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(28.dp))
                                }
                            }
                            "" -> Spacer(Modifier.size(72.dp))
                            else -> {
                                OutlinedButton(
                                    onClick = {
                                        mismatch = false
                                        if (phase == 1) {
                                            if (firstPin.length < 4) {
                                                firstPin += key
                                                if (firstPin.length == 4) phase = 2
                                            }
                                        } else {
                                            if (confirmPin.length < 4) {
                                                confirmPin += key
                                                if (confirmPin.length == 4) {
                                                    if (confirmPin == firstPin) {
                                                        viewModel.setupPin(confirmPin)
                                                    } else {
                                                        mismatch = true
                                                        firstPin = ""
                                                        confirmPin = ""
                                                        phase = 1
                                                    }
                                                }
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
}