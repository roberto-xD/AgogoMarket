package com.passioagogo.market.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(Singleton::class)
class PAInjectionUtils {
    @Provides
    fun providePADispatcherIO(): CoroutineDispatcher = Dispatchers.IO
}