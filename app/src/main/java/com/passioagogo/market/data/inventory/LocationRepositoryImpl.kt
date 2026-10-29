package com.passioagogo.market.data.inventory

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.inventory.remote.dto.LocationDto
import com.passioagogo.market.data.inventory.remote.dto.NewLocationDto
import com.passioagogo.market.domain.inventory.DireccionEnvioDraft
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.DireccionEnvioDraft
import com.passioagogo.market.domain.inventory.LocationDraft
import com.passioagogo.market.domain.inventory.DireccionEnvioDraft
import com.passioagogo.market.domain.inventory.LocationRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun LocationDto.toDomain() = Location(
    id = id, nombre = nombre, direccion = direccion, tipo = tipo, activo = activo,
    calle = calle, numero = numero, interior = interior, colonia = colonia,
    ciudad = ciudad, estado = estado, estadoCodigo = estadoCodigo, cp = cp,
    pais = pais, telefono = telefono, contacto = contacto,
    referencias = referencias, esOrigen = esOrigen,
)

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : LocationRepository {

    private companion object { const val TABLE = "locations" }

    override suspend fun getLocations(includeInactive: Boolean): DataResult<List<Location>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).select {
                    if (!includeInactive) filter { eq("activo", true) }
                    order("nombre", Order.ASCENDING)
                }.decodeList<LocationDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createLocation(draft: LocationDraft): DataResult<Location> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).insert(
                    NewLocationDto(draft.nombre, draft.direccion, draft.tipo)
                ) { select() }.decodeSingle<LocationDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updateLocation(location: Location): DataResult<Location> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TABLE).update({
                    set("nombre", location.nombre)
                    set("direccion", location.direccion)
                    set("tipo", location.tipo)
                    set("activo", location.activo)
                }) {
                    select()
                    filter { eq("id", location.id) }
                }.decodeSingle<LocationDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updateDireccionEnvio(
        locationId: String,
        draft: DireccionEnvioDraft,
    ): DataResult<Unit> = withContext(io) {
        safeSupabaseCall("updateDireccionEnvio") {
            postgrest.from(TABLE).update({
                // Vacío se guarda como null: el CHECK del CP rechaza la
                // cadena vacía, y null significa «sin capturar».
                set("calle", draft.calle?.trim()?.ifBlank { null })
                set("numero", draft.numero?.trim()?.ifBlank { null })
                set("interior", draft.interior?.trim()?.ifBlank { null })
                set("colonia", draft.colonia?.trim()?.ifBlank { null })
                set("ciudad", draft.ciudad?.trim()?.ifBlank { null })
                set("estado", draft.estado?.trim()?.ifBlank { null })
                set("estado_codigo", draft.estadoCodigo?.trim()?.uppercase()?.ifBlank { null })
                set("cp", draft.cp?.trim()?.ifBlank { null })
                set("telefono", draft.telefono?.trim()?.ifBlank { null })
                set("contacto", draft.contacto?.trim()?.ifBlank { null })
                set("referencias", draft.referencias?.trim()?.ifBlank { null })
            }) { filter { eq("id", locationId) } }
            Unit
        }
    }

    override suspend fun marcarComoOrigen(locationId: String): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall("marcarComoOrigen") {
                // El índice único parcial no admite dos orígenes a la vez:
                // hay que apagar el anterior antes de encender el nuevo.
                postgrest.from(TABLE).update({ set("es_origen", false) }) {
                    filter { eq("es_origen", true) }
                }
                postgrest.from(TABLE).update({ set("es_origen", true) }) {
                    filter { eq("id", locationId) }
                }
                Unit
            }
        }
}
