package com.passioagogo.market.di

import com.passioagogo.market.data.repository.PARepositoryCRUDImpl
import com.passioagogo.market.data.repository.PARepositoryPricesImpl
import com.passioagogo.market.domain.repository.PADBRepositoryCRUD
import com.passioagogo.market.domain.repository.PADBRepositoryPrices
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class PADBModuleBind{
    @Binds
    @Singleton
    abstract fun bindCRUDrepository(
        impl: PARepositoryCRUDImpl
    ): PADBRepositoryCRUD

    @Binds
    @Singleton
    abstract fun bindPriceRepository(
        impl: PARepositoryPricesImpl
    ): PADBRepositoryPrices
}
