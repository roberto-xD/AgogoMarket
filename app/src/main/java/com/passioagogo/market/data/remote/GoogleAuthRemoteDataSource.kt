package com.passioagogo.market.data.remote

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.passioagogo.market.R
import com.passioagogo.market.domain.model.GoogleAuthResult
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class GoogleAuthRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager: CredentialManager by lazy {
        CredentialManager.Companion.create(context)
    }

    suspend fun signIn(): GoogleAuthResult = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.google_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context as Activity
            )

            handleSignInResult(result)
        } catch (e: GetCredentialException) {
            when (e) {
                is GetCredentialCancellationException -> {
                    GoogleAuthResult.Cancelled
                }

                is GetCredentialInterruptedException -> {
                    GoogleAuthResult.Error("Autenticación interrumpida")
                }

                is NoCredentialException -> {
                    GoogleAuthResult.Error("No hay credenciales disponibles")
                }

                is GetCredentialProviderConfigurationException -> {
                    GoogleAuthResult.Error("Error de configuración del proveedor")
                }

                is GetCredentialUnknownException -> {
                    GoogleAuthResult.Error("Error desconocido: ${e.message}")
                }

                is GetCredentialUnsupportedException -> {
                    GoogleAuthResult.Error("Método de autenticación no soportado")
                }

                else -> {
                    GoogleAuthResult.Error(e.message ?: "Error en la autenticación")
                }
            }
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error inesperado")
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): GoogleAuthResult {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)

                        // Ahora necesitamos obtener el access token usando el ID token
                        val accessToken = exchangeIdTokenForAccessToken(googleIdTokenCredential.idToken)

                        GoogleAuthResult.Success
                    } catch (e: GoogleIdTokenParsingException) {
                        GoogleAuthResult.Error("Error al procesar credencial de Google: ${e.message}")
                    }
                } else {
                    GoogleAuthResult.Error("Tipo de credencial no esperado")
                }
            }
            else -> {
                GoogleAuthResult.Error("Credencial no reconocida")
            }
        }
    }

    private suspend fun exchangeIdTokenForAccessToken(idToken: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Configuración del cliente OAuth2
                val client = OkHttpClient()
                val requestBody = FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("client_id", context.getString(R.string.google_client_id))
                    .add(
                        "client_secret",
                        context.getString(R.string.google_client_secret)
                    ) // Necesitarás esto
                    .add("code", idToken)
                    .add(
                        "scope",
                        "https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive.file"
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://oauth2.googleapis.com/token")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Parsear la respuesta JSON para obtener el access_token
                    val json = JSONObject(responseBody ?: "")
                    json.optString("access_token")
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun signOut(): GoogleAuthResult = withContext(Dispatchers.IO) {
        try {
            // Con Credential Manager, simplemente limpiamos los datos locales
            // No hay un método específico de sign out como en GoogleSignIn
            GoogleAuthResult.Success
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error al cerrar sesión")
        }
    }

    suspend fun revokeAccess(): GoogleAuthResult = withContext(Dispatchers.IO) {
        try {
            // Para revocar el acceso, necesitamos hacer una llamada HTTP a Google
            val accessToken = getCurrentAccessToken()
            if (accessToken != null) {
                revokeTokenWithGoogle(accessToken)
            }
            GoogleAuthResult.Success
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "Error al revocar acceso")
        }
    }

    private suspend fun revokeTokenWithGoogle(token: String) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://oauth2.googleapis.com/revoke?token=$token")
                    .post(RequestBody.Companion.create(null, ""))
                    .build()

                client.newCall(request).execute()
            } catch (e: Exception) {
                // Log error but don't throw
            }
        }
    }

    private fun getCurrentAccessToken(): String? {
        // Esta función debería obtener el token del almacenamiento local
        // Se implementará en conjunto con GoogleAuthLocalDataSource
        return null
    }

    fun isAuthenticated(): Boolean {
        // Verificamos si hay un token válido almacenado localmente
        // Esta lógica se complementa con GoogleAuthLocalDataSource
        return false
    }
}