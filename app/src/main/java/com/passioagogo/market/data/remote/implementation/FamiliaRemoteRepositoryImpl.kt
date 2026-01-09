package com.passioagogo.market.data.remote.implementation

import com.passioagogo.market.data.remote.dto.SyncStatusDto
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

    override fun observeAll(): Flow<SyncStatusDto> = callbackFlow {
        val channel = supabaseClient.channel("sync-channel")

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "sync_status"
            filter = "id=eq.familias"  // Solo escuchar cambios de familias
        }

        channel.subscribe()

        changeFlow.collect {
            val status = supabaseClient.from("sync_status")
                .select { filter { eq("id", "familias") } }
                .decodeSingle<SyncStatusDto>()
            send(status)
        }

        awaitClose { launch { channel.unsubscribe() } }
    }
}