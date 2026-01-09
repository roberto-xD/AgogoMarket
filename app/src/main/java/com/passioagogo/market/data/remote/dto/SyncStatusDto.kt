package com.passioagogo.market.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncStatusDto(
    @SerialName("id")
    val id: String,

    @SerialName("lastUpdate")
    val lastUpdate: Long? = null,

    @SerialName("action")
    val  action: String,

    @SerialName("version")
    val  version: Int,

    @SerialName("recordId")
    val  recordId: String,
)
