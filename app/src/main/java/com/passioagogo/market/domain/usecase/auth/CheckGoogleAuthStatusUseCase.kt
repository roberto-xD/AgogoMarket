package com.passioagogo.market.domain.usecase.auth

import com.passioagogo.market.domain.GoogleAuthRepository
import jakarta.inject.Inject

class CheckGoogleAuthStatusUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isAuthenticated()
    }
}