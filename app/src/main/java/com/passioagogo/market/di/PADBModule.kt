package com.passioagogo.market.di

import com.passioagogo.market.data.repository.PARepositoryImpl
import com.passioagogo.market.domain.repository.PADBRepository
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
    abstract fun bindRepository(
        impl: PARepositoryImpl
    ): PADBRepository
}
