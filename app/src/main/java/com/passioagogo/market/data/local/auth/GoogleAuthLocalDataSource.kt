package com.passioagogo.market.data.local.auth

import android.content.SharedPreferences
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import jakarta.inject.Inject

class GoogleAuthLocalDataSource @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "google_access_token"
        private const val KEY_REFRESH_TOKEN = "google_refresh_token"
        private const val KEY_ID_TOKEN = "google_id_token"
        private const val KEY_TOKEN_EXPIRY = "google_token_expiry"
        private const val KEY_USER_EMAIL = "google_user_email"
        private const val KEY_USER_NAME = "google_user_name"
        private const val KEY_USER_PHOTO = "google_user_photo"
        private const val KEY_USER_ID = "google_user_id"
    }

    fun saveAuthData(
        accessToken: String,
        refreshToken: String?,
        idToken: String,
        expiryTime: Long,
        email: String,
        name: String?,
        photoUrl: String?,
        userId: String
    ) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ID_TOKEN, idToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHOTO, photoUrl)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun saveGoogleIdTokenCredential(credential: GoogleIdTokenCredential) {
        val expiryTime = System.currentTimeMillis() + (3600 * 1000) // 1 hora por defecto

        sharedPreferences.edit()
            .putString(KEY_ID_TOKEN, credential.idToken)
            .putString(KEY_USER_EMAIL, credential.id)
            .putString(KEY_USER_NAME, credential.displayName)
            .putString(KEY_USER_PHOTO, credential.profilePictureUri?.toString())
            .putString(KEY_USER_ID, credential.id)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun getIdToken(): String? = sharedPreferences.getString(KEY_ID_TOKEN, null)

    fun getTokenExpiry(): Long = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)

    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)

    fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)

    fun getUserPhoto(): String? = sharedPreferences.getString(KEY_USER_PHOTO, null)

    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

    fun isTokenExpired(): Boolean {
        val expiryTime = getTokenExpiry()
        return System.currentTimeMillis() >= expiryTime
    }

    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ID_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PHOTO)
            .remove(KEY_USER_ID)
            .apply()
    }

    fun hasValidAuth(): Boolean {
        return (getAccessToken() != null || getIdToken() != null) && !isTokenExpired()
    }
}