package com.passioagogo.market.data.local
import PADataConstants.NAME_TABLE
import android.content.Context
import androidx.room.Room
import com.passioagogo.market.data.local.dao.PAProductPricesDao
import com.passioagogo.market.data.local.dao.PAProductsDaoCRUD
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PADatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PADataBase {
        return Room.databaseBuilder(
            context = context,
            klass = PADataBase::class.java,
            name = NAME_TABLE
        ).build()
    }

    @Singleton
    @Provides
    fun provideUserDao(
        db: PADataBase
    ): PAProductsDaoCRUD {
        return db.productDao()
    }

    @Singleton
    @Provides
    fun providePricesDao(
        db: PADataBase
    ): PAProductPricesDao {
        return db.priceDao()
    }
}

