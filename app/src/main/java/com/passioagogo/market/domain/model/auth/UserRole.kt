package com.passioagogo.market.domain.model.auth

enum class UserRole(val level: Int) {
    USER(1),
    ADMIN(2),
    SUPER_USER(3);

    companion object {
        fun fromString(role: String): UserRole {
            return when (role.lowercase()) {
                "super_user", "superuser" -> SUPER_USER
                "admin", "administrator" -> ADMIN
                else -> USER
            }
        }
    }

    fun hasPermission(requiredRole: UserRole): Boolean {
        return this.level >= requiredRole.level
    }
}
