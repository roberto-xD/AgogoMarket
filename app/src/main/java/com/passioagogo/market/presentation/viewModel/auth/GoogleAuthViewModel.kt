package com.passioagogo.market.presentation.viewModel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.model.GoogleAuthResult
import com.passioagogo.market.domain.usecase.auth.CheckGoogleAuthStatusUseCase
import com.passioagogo.market.domain.usecase.auth.GetCurrentGoogleAccountUseCase
import com.passioagogo.market.domain.usecase.auth.RefreshGoogleTokenUseCase
import com.passioagogo.market.domain.usecase.auth.SignInWithGoogleUseCase
import com.passioagogo.market.domain.usecase.auth.SignOutFromGoogleUseCase
import com.passioagogo.market.presentation.uiState.GoogleAuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GoogleAuthViewModel @Inject constructor(
    private val signInUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutFromGoogleUseCase,
    private val checkAuthStatusUseCase: CheckGoogleAuthStatusUseCase,
    private val getCurrentAccountUseCase: GetCurrentGoogleAccountUseCase,
    private val refreshTokenUseCase: RefreshGoogleTokenUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(GoogleAuthUiState())
    val authState: StateFlow<GoogleAuthUiState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun signIn() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signInUseCase()) {
                is GoogleAuthResult.Success -> {
                    val account = getCurrentAccountUseCase()
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentAccount = account,
                        error = null
                    )
                }
                is GoogleAuthResult.Cancelled -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Autenticación cancelada por el usuario"
                    )
                }
                is GoogleAuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is GoogleAuthResult.NotAuthenticated -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        currentAccount = null,
                        error = "No se pudo autenticar"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            when (val result = signOutUseCase()) {
                is GoogleAuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        currentAccount = null,
                        error = null
                    )
                }
                is GoogleAuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val isAuthenticated = checkAuthStatusUseCase()
            val account = if (isAuthenticated) getCurrentAccountUseCase() else null

            _authState.value = _authState.value.copy(
                isAuthenticated = isAuthenticated,
                currentAccount = account,
                isLoading = false
            )
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            when (val result = refreshTokenUseCase()) {
                is GoogleAuthResult.Success -> {
                    checkAuthStatus()
                }
                is GoogleAuthResult.NotAuthenticated -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        currentAccount = null,
                        isLoading = false
                    )
                }
                is GoogleAuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
