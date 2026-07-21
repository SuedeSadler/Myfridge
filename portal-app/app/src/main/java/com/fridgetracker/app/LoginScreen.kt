package com.fridgetracker.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

/**
 * One-time sign-in for the shared household account. Both the Portal and
 * the phone web view sign into the SAME account, so Supabase's row-level
 * security shows the same inventory on both — this is why we moved off
 * anonymous auth (each anonymous session was a different, private user).
 */
@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Fridge Tracker", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in with the household account",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
            )

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = AppColors.danger, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        try {
                            SupabaseClientProvider.client.auth.signInWith(Email) {
                                this.email = email.trim()
                                this.password = password
                            }
                            onSignedIn()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Sign in failed"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Sign in", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
