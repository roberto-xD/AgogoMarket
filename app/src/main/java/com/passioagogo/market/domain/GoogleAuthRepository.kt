package com.passioagogo.market.domain

import com.passioagogo.market.domain.model.GoogleAccount
import com.passioagogo.market.domain.model.GoogleAuthResult

interface GoogleAuthRepository {
    suspend fun signIn(): GoogleAuthResult
    suspend fun signOut(): GoogleAuthResult
    suspend fun isAuthenticated(): Boolean
    suspend fun getCurrentAccount(): GoogleAccount?
    suspend fun refreshAccessToken(): GoogleAuthResult
    suspend fun revokeAccess(): GoogleAuthResult
}