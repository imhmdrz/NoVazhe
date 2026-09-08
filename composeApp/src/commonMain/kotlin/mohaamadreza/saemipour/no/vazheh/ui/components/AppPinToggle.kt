package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.AuthRepository
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinState
import mohaamadreza.saemipour.no.vazheh.pinning.rememberAppPinner
import org.koin.compose.koinInject

@Composable
fun AppPinToggle(modifier: Modifier = Modifier) {
    val pinner = rememberAppPinner()
    val authRepository = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()
    var isPinned by rememberAppPinState(pinner)
    var showUnlockPrompt by remember { mutableStateOf(false) }
    var isVerifyingPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.9f), CircleShape)
            .border(1.dp, Color.Black, CircleShape)
            .clickable(enabled = !isVerifyingPassword) {
                if (isPinned) {
                    passwordError = null
                    showUnlockPrompt = true
                } else if (!isPinned) {
                    scope.launch {
                        pinner.pinAndAwait()
                        isPinned = pinner.isPinned()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPinned) "🔒" else "🔓",
            fontSize = 20.sp
        )
    }

    if (showUnlockPrompt) {
        PasswordPromptDialog(
            isLoading = isVerifyingPassword,
            errorMessage = passwordError,
            title = "باز کردن قفل",
            description = "برای باز کردن قفل، رمز عبور حساب خود را وارد کنید.",
            onDismiss = {
                if (!isVerifyingPassword) {
                    showUnlockPrompt = false
                    passwordError = null
                }
            },
            onConfirm = { password ->
                if (!isVerifyingPassword) {
                    isVerifyingPassword = true
                    passwordError = null
                    scope.launch {
                        authRepository.verifyPassword(password).fold(
                            onSuccess = { verified ->
                                if (verified) {
                                    showUnlockPrompt = false
                                    isVerifyingPassword = false
                                    pinner.unpin()
                                    isPinned = pinner.isPinned()
                                } else {
                                    isVerifyingPassword = false
                                    passwordError = "رمز عبور اشتباه است"
                                }
                            },
                            onFailure = { exception ->
                                isVerifyingPassword = false
                                passwordError = "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            }
                        )
                    }
                }
            }
        )
    }
}
