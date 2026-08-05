package com.passioagogo.market.data.inventory

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.data.inventory.remote.dto.LocationDto
import com.passioagogo.market.data.inventory.remote.dto.NewLocationDto
import com.passioagogo.market.domain.inventory.Location
import com.passioagogo.market.domain.inventory.LocationDraft
import com.passioagogo.market.domain.inventory.LocationRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun LocationDto.toDomain() = Location(
    id = id, nombre = nombre, direccion = direccion, tipo = tipo, activo = activo,
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
}
