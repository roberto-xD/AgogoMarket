package com.passioagogo.market.di

import android.content.Context
import android.content.SharedPreferences
import com.passioagogo.market.ui.utils.ImageFileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideImageFileManager(
        @ApplicationContext context: Context
    ): ImageFileManager {
        return ImageFileManager(context)
    }
}