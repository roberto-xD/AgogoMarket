package com.passioagogo.market.data.remote.repository

import com.passioagogo.market.data.remote.dto.FamiliaRemoteDto

interface FamiliaRemoteRepository {
    suspend fun getAll(): Result<List<FamiliaRemoteDto>>
    suspend fun getById(id: String): Result<FamiliaRemoteDto?>
    suspend fun insert(familia: FamiliaRemoteDto): Result<FamiliaRemoteDto>
    suspend fun update(familia: FamiliaRemoteDto): Result<FamiliaRemoteDto>
    suspend fun delete(id: String): Result<Unit>
}