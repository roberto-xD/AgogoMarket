package com.passioagogo.market.domain.repository

import com.passioagogo.market.domain.model.auth.AuthResult
import com.passioagogo.market.domain.model.auth.AuthState
import com.passioagogo.market.domain.model.auth.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthState>

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    suspend fun signInWithGoogle(idToken: String): AuthResult

    suspend fun signUp(email: String, password: String, displayName: String?): AuthResult

    suspend fun signOut(): Result<Unit>

    suspend fun getCurrentUser(): User?

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun updateUserRole(userId: String, role: String): Result<Unit>
}