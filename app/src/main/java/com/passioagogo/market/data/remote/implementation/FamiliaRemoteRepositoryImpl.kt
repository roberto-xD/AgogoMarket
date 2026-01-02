package com.passioagogo.market.data.remote.implementation

import com.passioagogo.market.data.remote.dto.FamiliaRemoteDto
import com.passioagogo.market.data.remote.repository.FamiliaRemoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FamiliaRemoteRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : FamiliaRemoteRepository {

    private val table = "familias"

    override suspend fun getAll(): Result<List<FamiliaRemoteDto>> = runCatching {
        supabaseClient.from(table)
            .select()
            .decodeList<FamiliaRemoteDto>()
    }

    override suspend fun getById(id: String): Result<FamiliaRemoteDto?> = runCatching {
        supabaseClient.from(table)
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<FamiliaRemoteDto>()
    }

    override suspend fun insert(familia: FamiliaRemoteDto): Result<FamiliaRemoteDto> = runCatching {
        supabaseClient.from(table)
            .insert(familia) {
                select()
            }
            .decodeSingle<FamiliaRemoteDto>()
    }

    override suspend fun update(familia: FamiliaRemoteDto): Result<FamiliaRemoteDto> = runCatching {
        supabaseClient.from(table)
            .update(familia) {
                filter { eq("id", familia.id) }
                select()
            }
            .decodeSingle<FamiliaRemoteDto>()
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        supabaseClient.from(table)
            .delete {
                filter { eq("id", id) }
            }
    }

    override fun observeAll(): Flow<List<FamiliaRemoteDto>> = callbackFlow {
        val initial = supabaseClient.from(table).select().decodeList<FamiliaRemoteDto>()
        send(initial)
        val channel = supabaseClient.channel("familias-channel")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public"){
            table = "familias"
        }
        channel.subscribe()
        changeFlow.collect { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val updated = supabaseClient.from(table).select().decodeList<FamiliaRemoteDto>()
                    send(updated)
                }
                is PostgresAction.Update -> { /* registro actualizado */ }
                is PostgresAction.Delete -> { /* registro eliminado */ }
                is PostgresAction.Select -> { /* select */ }
            }
        }
        awaitClose {
            launch { channel.unsubscribe() }
        }
    }
}