package com.passioagogo.market.domain.model

sealed class GoogleAuthResult {
    object Success : GoogleAuthResult()
    object Cancelled : GoogleAuthResult()
    data class Error(val message: String) : GoogleAuthResult()
    object NotAuthenticated : GoogleAuthResult()
}