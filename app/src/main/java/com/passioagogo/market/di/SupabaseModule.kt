package com.passioagogo.market.di

import android.content.Context
import com.passioagogo.market.BuildConfig
import com.passioagogo.market.data.remote.implementation.FamiliaRemoteRepositoryImpl
import com.passioagogo.market.data.remote.repository.FamiliaRemoteRepository
import com.passioagogo.market.data.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

// di/SupabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "app"
                host = "supabase.com"
            }
            install(Storage)
            install(Realtime)
            install(Postgrest)
        }
    }

    /**
     * Proporciona el módulo de autenticación
     */
    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    /**
     * Proporciona el módulo de base de datos
     */
    @Provides
    @Singleton
    fun provideSupabaseDatabase(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        @ApplicationContext context: Context,
        storage: Storage
    ): StorageRepository = StorageRepository(
        context = context,
        storage = storage
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FamiliaRemoteModule {

    @Binds
    @Singleton
    abstract fun bindFamiliaRemoteRepository(
        impl: FamiliaRemoteRepositoryImpl
    ): FamiliaRemoteRepository
}