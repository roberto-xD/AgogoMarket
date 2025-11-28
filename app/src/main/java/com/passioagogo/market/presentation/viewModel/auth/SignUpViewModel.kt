package com.passioagogo.market.presentation.viewModel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.model.auth.AuthResult
import com.passioagogo.market.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SignUpEvent>()
    val events = _events.asSharedFlow()

    fun onDisplayNameChange(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                passwordStrength = calculatePasswordStrength(password)
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onSignUpClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            when (val result = signUpUseCase(
                email = state.email,
                password = state.password,
                confirmPassword = state.confirmPassword,
                displayName = state.displayName.ifBlank { null }
            )) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SignUpEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                    // Si es mensaje de verificación, mostrarlo diferente
                    if (result.message.contains("verifica")) {
                        _events.emit(SignUpEvent.ShowVerificationMessage(result.message))
                    }
                }
            }
        }
    }

    fun onBackToLoginClick() {
        viewModelScope.launch {
            _events.emit(SignUpEvent.NavigateBack)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE

        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 1 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.NONE
)

enum class PasswordStrength {
    NONE, WEAK, MEDIUM, STRONG
}

sealed interface SignUpEvent {
    data object NavigateToHome : SignUpEvent
    data object NavigateBack : SignUpEvent
    data class ShowVerificationMessage(val message: String) : SignUpEvent
}