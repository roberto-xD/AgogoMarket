package com.passioagogo.market.data.local
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.data.local.dao.PAProductsDaoCRUD
import androidx.room.Database
import androidx.room.RoomDatabase
import com.passioagogo.market.data.local.dao.PAProductPricesDao
import com.passioagogo.market.data.local.entity.PAPriceEntity
import com.passioagogo.market.data.local.entity.PAPurchasesEntity
import com.passioagogo.market.data.local.entity.PASalesEntity

@Database(
    entities = [
        PAProductEntity::class,
        PAPriceEntity::class,
        PAPurchasesEntity::class,
        PASalesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PADataBase : RoomDatabase(){
    abstract fun productDao() : PAProductsDaoCRUD
    abstract fun priceDao() : PAProductPricesDao
}