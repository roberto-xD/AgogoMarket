package com.passioagogo.market.data.local.entity

import PADataConstants.PRODUCT_STATISTICS_CROSS_REF
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = PRODUCT_STATISTICS_CROSS_REF,
    foreignKeys = [
        ForeignKey(entity = PAProductEntity::class, parentColumns = ["ProductId"], childColumns = ["productId"]),
        ForeignKey(entity = PAStatistics::class, parentColumns = ["statisticsId"], childColumns = ["statisticsId"]),
    ]
)
data class PAStatisticsProductCrossRefEntity(
    val productId: Int,
    val statisticsId: Int
)
