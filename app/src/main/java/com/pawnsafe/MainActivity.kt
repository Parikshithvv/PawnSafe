package com.pawnsafe

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawnsafe.presentation.auth.AuthViewModel
import com.pawnsafe.presentation.auth.LockScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val authViewModel: AuthViewModel = viewModel()
                    val pinEnabled by authViewModel.pinEnabled.collectAsState()
                    var unlocked by remember { mutableStateOf(false) }

                    when {
                        !pinEnabled -> MainScreen()
                        unlocked    -> MainScreen()
                        else        -> LockScreen(onUnlocked = { unlocked = true })
                    }
                }
            }
        }
    }
}