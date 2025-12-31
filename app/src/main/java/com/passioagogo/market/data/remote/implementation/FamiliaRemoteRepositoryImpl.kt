package com.passioagogo.market.data.remote.implementation

import com.passioagogo.market.data.remote.dto.FamiliaRemoteDto
import com.passioagogo.market.data.remote.repository.FamiliaRemoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
}