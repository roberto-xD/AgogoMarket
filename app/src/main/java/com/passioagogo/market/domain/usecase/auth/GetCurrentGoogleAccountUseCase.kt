package com.passioagogo.market.domain.usecase.auth

import com.passioagogo.market.domain.model.GoogleAccount
import com.passioagogo.market.domain.GoogleAuthRepository
import jakarta.inject.Inject

class GetCurrentGoogleAccountUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(): GoogleAccount? {
        return repository.getCurrentAccount()
    }
}