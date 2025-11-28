package com.passioagogo.market.domain.model.auth

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val role: UserRole,
    val createdAt: Long,
    val lastSignInAt: Long?
)