package com.passioagogo.market.domain.usecase.auth

import android.util.Patterns
import com.passioagogo.market.domain.model.auth.AuthResult
import com.passioagogo.market.domain.model.auth.AuthState
import com.passioagogo.market.domain.model.auth.User
import com.passioagogo.market.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank()) {
            return AuthResult.Error("El correo electrónico es requerido")
        }
        if (password.isBlank()) {
            return AuthResult.Error("La contraseña es requerida")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error("El correo electrónico no es válido")
        }
        return repository.signInWithEmail(email.trim(), password)
    }
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        if (idToken.isBlank()) {
            return AuthResult.Error("Token de Google inválido")
        }
        return repository.signInWithGoogle(idToken)
    }
}

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String?
    ): AuthResult {
        if (email.isBlank()) {
            return AuthResult.Error("El correo electrónico es requerido")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthResult.Error("El correo electrónico no es válido")
        }
        if (password.isBlank()) {
            return AuthResult.Error("La contraseña es requerida")
        }
        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }
        if (password != confirmPassword) {
            return AuthResult.Error("Las contraseñas no coinciden")
        }
        return repository.signUp(email.trim(), password, displayName?.trim())
    }
}

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.signOut()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}

class ObserveAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> {
        return repository.authState
    }
}

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo electrónico es requerido"))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("El correo electrónico no es válido"))
        }
        return repository.resetPassword(email.trim())
    }
}