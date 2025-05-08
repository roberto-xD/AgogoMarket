package com.passioagogo.market.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ ForeignKey(
        entity = PAProductEntity::class,
        parentColumns = ["productId"],
        childColumns = ["productIdRef"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PAStatistics(
    @PrimaryKey(autoGenerate = true) val statisticsId: Int = 0,
    var productIdRef : Long = 0,
    val last_sale : String = "",
)