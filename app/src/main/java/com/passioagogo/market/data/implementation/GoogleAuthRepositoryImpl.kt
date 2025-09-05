package com.passioagogo.market.data.implementation

import android.content.Context
import com.passioagogo.market.R
import com.passioagogo.market.data.local.auth.GoogleAuthLocalDataSource
import com.passioagogo.market.data.remote.GoogleAuthRemoteDataSource
import com.passioagogo.market.domain.GoogleAuthRepository
import com.passioagogo.market.domain.model.GoogleAccount
import com.passioagogo.market.domain.model.GoogleAuthResult
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Singleton
class GoogleAuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: GoogleAuthRemoteDataSource,
    private val localDataSource: GoogleAuthLocalDataSource,
    @ApplicationContext private val context: Context
) : GoogleAuthRepository {

    override suspend fun signIn(): GoogleAuthResult {
        return try {
            // Primero verificamos si ya hay una sesión válida
            if (isAuthenticated()) {
                return GoogleAuthResult.Success
            }

            remoteDataSource.signIn()
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error durante la autenticación")
        }
    }

    override suspend fun signOut(): GoogleAuthResult {
        return try {
            val result = remoteDataSource.signOut()
            if (result is GoogleAuthResult.Success) {
                localDataSource.clearAuthData()
            }
            result
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error al cerrar sesión")
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return localDataSource.hasValidAuth()
    }

    override suspend fun getCurrentAccount(): GoogleAccount? {
        return if (isAuthenticated()) {
            GoogleAccount(
                email = localDataSource.getUserEmail() ?: "",
                displayName = localDataSource.getUserName(),
                photoUrl = localDataSource.getUserPhoto(),
                accessToken = localDataSource.getAccessToken() ?: "",
                refreshToken = localDataSource.getRefreshToken()
            )
        } else {
            null
        }
    }

    override suspend fun refreshAccessToken(): GoogleAuthResult {
        return try {
            val refreshToken = localDataSource.getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                return GoogleAuthResult.NotAuthenticated
            }

            // Implementar lógica de refresh usando OAuth2
            refreshTokenWithGoogle(refreshToken)
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error al refrescar token")
        }
    }

    private suspend fun refreshTokenWithGoogle(refreshToken: String): GoogleAuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val requestBody = FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("client_id", context.getString(R.string.google_client_id))
                    .add("client_secret", context.getString(R.string.google_client_secret))
                    .add("refresh_token", refreshToken)
                    .build()

                val request = Request.Builder()
                    .url("https://oauth2.googleapis.com/token")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody ?: "")
                    val newAccessToken = json.optString("access_token")
                    val expiresIn = json.optInt("expires_in", 3600)

                    if (newAccessToken.isNotEmpty()) {
                        // Actualizar el token local
                        val currentAccount = getCurrentAccount()
                        currentAccount?.let { account ->
                            localDataSource.saveAuthData(
                                accessToken = newAccessToken,
                                refreshToken = refreshToken,
                                idToken = localDataSource.getIdToken() ?: "",
                                expiryTime = System.currentTimeMillis() + (expiresIn * 1000),
                                email = account.email,
                                name = account.displayName,
                                photoUrl = account.photoUrl,
                                userId = localDataSource.getUserId() ?: ""
                            )
                        }
                    }
                    GoogleAuthResult.Success
                } else {
                    GoogleAuthResult.Error("Error al refrescar token")
                }
            } catch (e: Exception) {
                GoogleAuthResult.Error(e.message ?: "Error al refrescar token")
            }
        }
    }

    override suspend fun revokeAccess(): GoogleAuthResult {
        return try {
            val result = remoteDataSource.revokeAccess()
            if (result is GoogleAuthResult.Success) {
                localDataSource.clearAuthData()
            }
            result
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error al revocar acceso")
        }
    }
}