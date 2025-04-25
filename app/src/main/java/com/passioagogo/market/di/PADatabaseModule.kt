package com.passioagogo.market.data.local
import PADataConstants.NAME_TABLE
import android.content.Context
import androidx.room.Room
import com.passioagogo.market.data.local.dao.PAProductsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PADatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PADataBase {
        return Room.databaseBuilder(
            context = context,
            klass = PADataBase::class.java,
            name = NAME_TABLE
        ).build()
    }

    @Provides
    fun provideUserDao(
        db: PADataBase
    ): PAProductsDao {
        return db.productDao()
    }
}

