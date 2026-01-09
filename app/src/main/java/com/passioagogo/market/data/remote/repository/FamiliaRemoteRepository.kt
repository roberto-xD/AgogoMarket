package com.passioagogo.market.data.remote.repository

import com.passioagogo.market.data.remote.dto.SyncStatusDto
import kotlinx.coroutines.flow.Flow

interface FamiliaRemoteRepository {
    fun observeAll(): Flow<SyncStatusDto>
}