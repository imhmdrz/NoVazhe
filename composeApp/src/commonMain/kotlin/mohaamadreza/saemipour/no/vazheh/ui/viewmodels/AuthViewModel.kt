package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.AuthRepository

data class AuthUiState(
        val username: String = "",
        val password: String = "",
        val displayName: String = "",
        val isLoginMode: Boolean = true,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.update { it.copy(displayName = newDisplayName, errorMessage = null) }
    }

    fun onToggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null) }
    }

    fun onSubmit(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Basic validation
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "نام کاربری نمی‌تواند خالی باشد") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "رمز عبور نمی‌تواند خالی باشد") }
            return
        }
        if (!currentState.isLoginMode && currentState.displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "نام نمایشی نمی‌تواند خالی باشد") }
            return
        }
        if (currentState.username.length < 3) {
            _uiState.update { it.copy(errorMessage = "نام کاربری باید حداقل ۳ کاراکتر باشد") }
            return
        }
        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result =
                    if (currentState.isLoginMode) {
                        authRepository.login(currentState.username, currentState.password)
                    } else {
                        authRepository.register(
                                currentState.username,
                                currentState.password,
                                currentState.displayName
                        )
                    }

            result.fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isLoading = false) }
                        if (response.success) {
                            onSuccess()
                        } else {
                            _uiState.update { it.copy(errorMessage = response.message) }
                        }
                    },
                    onFailure = { exception ->
                        AppLogger.d("mhmdrz" , "exception: ${exception.message}")
                        _uiState.update {
                            it.copy(
                                    isLoading = false,
                                    errorMessage =
                                            "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                            )
                        }
                    }
            )
        }
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun logout() {
        authRepository.logout()
    }

    /**
     * Force logout without notifying AuthStateManager (used when handling 401)
     * خروج اجباری بدون اطلاع‌رسانی (برای مدیریت خطای 401)
     */
    fun forceLogout() {
        authRepository.forceLogout()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
