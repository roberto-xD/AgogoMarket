package com.passioagogo.market.presentation.uiState

import com.passioagogo.market.domain.model.GoogleAccount

data class GoogleAuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentAccount: GoogleAccount? = null,
    val error: String? = null
)
