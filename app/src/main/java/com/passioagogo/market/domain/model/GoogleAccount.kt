package com.passioagogo.market.domain.model

data class GoogleAccount(
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val accessToken: String,
    val refreshToken: String?
)