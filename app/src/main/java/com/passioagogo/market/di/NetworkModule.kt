package com.passioagogo.market.di

import com.google.android.datatransport.runtime.dagger.Provides
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}