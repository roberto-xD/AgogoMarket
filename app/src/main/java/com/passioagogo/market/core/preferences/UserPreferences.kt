package com.passioagogo.market.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

/**
 * Preferencias locales del usuario. La "ubicación activa" es la que el
 * ADMIN eligió por última vez — compartida entre Inventario y Vender.
 * El vendedor no la usa: su tienda viene fija del profile.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyActiveLocation = stringPreferencesKey("active_location_id")

    /** null = sin selección guardada (o "todas"). */
    val activeLocationId: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[keyActiveLocation] }

    suspend fun setActiveLocation(locationId: String?) {
        context.dataStore.edit { prefs ->
            if (locationId == null) prefs.remove(keyActiveLocation)
            else prefs[keyActiveLocation] = locationId
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule
