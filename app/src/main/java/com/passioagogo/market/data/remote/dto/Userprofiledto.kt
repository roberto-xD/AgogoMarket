package com.passioagogo.market.data.remote.dto

import com.passioagogo.market.domain.model.auth.User
import com.passioagogo.market.domain.model.auth.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("role")
    val role: String = "user",

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

fun UserProfileDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        role = UserRole.fromString(role),
        createdAt = parseTimestamp(createdAt),
        lastSignInAt = updatedAt?.let { parseTimestamp(it) }
    )
}

private fun parseTimestamp(timestamp: String): Long {
    return try {
        java.time.Instant.parse(timestamp).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}