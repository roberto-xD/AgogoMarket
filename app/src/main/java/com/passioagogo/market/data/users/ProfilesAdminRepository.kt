package com.passioagogo.market.data.users

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.auth.ProfileDto
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.common.UserRole
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Gestión de perfiles (solo admin puede cambiar rol/tienda/activo:
 * lo garantizan fn_profiles_guard + RLS). La CREACIÓN de usuarios no
 * es posible desde el cliente (requiere service role): se hace por
 * registro o desde el dashboard de Supabase.
 */
interface ProfilesAdminRepository {
    suspend fun getProfiles(): DataResult<List<Profile>>
    suspend fun updateProfile(
        id: String,
        nombre: String,
        rol: UserRole,
        locationId: String?,
        activo: Boolean,
    ): DataResult<Profile>
}

@Singleton
class ProfilesAdminRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ProfilesAdminRepository {

    private companion object { const val TABLE = "profiles" }

    override suspend fun getProfiles(): DataResult<List<Profile>> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).select {
                order("nombre", Order.ASCENDING)
            }.decodeList<ProfileDto>()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateProfile(
        id: String,
        nombre: String,
        rol: UserRole,
        locationId: String?,
        activo: Boolean,
    ): DataResult<Profile> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TABLE).update({
                set("nombre", nombre)
                set("rol", rol)
                set("location_id", locationId)
                set("activo", activo)
            }) {
                select()
                filter { eq("id", id) }
            }.decodeSingle<ProfileDto>()
        }.map { it.toDomain() }
    }
}
