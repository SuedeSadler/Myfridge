package com.fridgetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import io.github.jan.supabase.gotrue.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // null = still checking, false = needs login, true = signed in.
            // Supabase persists the session locally, so this stays true
            // across app restarts once you've signed in the first time.
            var isSignedIn by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                val auth = SupabaseClientProvider.client.auth
                isSignedIn = auth.currentSessionOrNull() != null
            }

            MaterialTheme(
                typography = AppTypography,
                colorScheme = lightColorScheme(
                    primary = AppColors.accent,
                    background = AppColors.background,
                    surface = AppColors.surface
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (isSignedIn) {
                        null -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        false -> {
                            LoginScreen(onSignedIn = { isSignedIn = true })
                        }
                        true -> {
                            InventoryScreen()
                        }
                    }
                }
            }
        }
    }
}
