package com.passioagogo.market.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.passioagogo.market.data.catalog.local.CatalogDao
import com.passioagogo.market.data.catalog.local.CategoryEntity
import com.passioagogo.market.data.catalog.local.ProductEntity
import com.passioagogo.market.data.catalog.local.ProductVariantEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PADatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PADatabase =
        Room.databaseBuilder(context, PADatabase::class.java, "passion_agogo.db")
            // Caché reconstruible desde Supabase: ante cambio de esquema, se regenera
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCatalogDao(db: PADatabase): CatalogDao = db.catalogDao()
}
