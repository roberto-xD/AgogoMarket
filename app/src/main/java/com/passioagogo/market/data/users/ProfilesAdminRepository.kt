package com.passioagogo.market.data.users

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.auth.ProfileDto
import com.passioagogo.market.domain.auth.Profile
import com.passioagogo.market.domain.common.UserRole
import io.github.jan.supabase.functions.Functions
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
data class NuevoUsuario(
    val email: String,
    val password: String,
    val nombre: String,
    val rol: UserRole,
    val locationId: String?,
)

interface ProfilesAdminRepository {
    suspend fun getProfiles(): DataResult<List<Profile>>

    /**
     * Crea un usuario de personal a través de la Edge Function `create-staff`.
     * No se hace desde el cliente porque exige la service_role key, que no
     * puede vivir dentro del APK.
     */
    suspend fun crearUsuario(nuevo: NuevoUsuario): DataResult<Unit>
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
    private val functions: Functions,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ProfilesAdminRepository {

    private companion object {
        const val TABLE = "profiles"
        const val FN_CREAR = "create-staff"
    }

    override suspend fun crearUsuario(nuevo: NuevoUsuario): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                val respuesta = functions.invoke(
                    function = FN_CREAR,
                    body = buildJsonObject {
                        put("email", nuevo.email.trim())
                        put("password", nuevo.password)
                        put("nombre", nuevo.nombre.trim())
                        put("rol", if (nuevo.rol == UserRole.PROMOTOR) "promotor" else "vendedor")
                        nuevo.locationId?.let { put("location_id", it) }
                    },
                )
                val cuerpo = respuesta.body<JsonObject>()
                // La función responde 4xx con {"error": "..."} para los
                // casos previsibles: correo repetido, contraseña corta…
                cuerpo["error"]?.jsonPrimitive?.content?.let { error(it) }
                Unit
            }
        }

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
