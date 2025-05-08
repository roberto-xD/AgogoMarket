package com.passioagogo.market.data.local
import com.passioagogo.market.data.local.entity.PAProductEntity
import com.passioagogo.market.data.local.dao.PAProductsDao
import androidx.room.Database
import androidx.room.RoomDatabase
import com.passioagogo.market.data.local.entity.PAStatistics

@Database(
    entities = [
        PAProductEntity::class,
        PAStatistics::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class PADataBase : RoomDatabase(){
    abstract fun productDao() : PAProductsDao
}