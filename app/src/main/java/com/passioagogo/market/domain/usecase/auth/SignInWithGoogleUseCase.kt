package com.passioagogo.market.domain.usecase.auth

import com.passioagogo.market.domain.model.GoogleAuthResult
import com.passioagogo.market.domain.GoogleAuthRepository
import jakarta.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(): GoogleAuthResult {
        return repository.signIn()
    }
}