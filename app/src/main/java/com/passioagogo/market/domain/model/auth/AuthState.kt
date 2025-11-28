package com.passioagogo.market.domain.model.auth

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String, val exception: Throwable? = null) : AuthResult
}