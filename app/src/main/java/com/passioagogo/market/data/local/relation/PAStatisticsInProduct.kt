package com.passioagogo.market.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.data.local.entity.PAStatistics
import com.passioagogo.market.data.local.entity.PAStatisticsProductCrossRefEntity

data class PAProductWithStatistics(
    @Embedded val product: PAProductEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "productIdRef",
//        entity = PAStatistics::class,
//        associateBy = Junction(PAStatisticsProductCrossRefEntity::class)
    )
    val statistics: PAStatistics
)