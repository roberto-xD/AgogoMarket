package com.passioagogo.market.di

import android.content.Context
import com.passioagogo.market.data.implementation.CatalogoRepositoryImpl
import com.passioagogo.market.domain.repository.CatalogoRepository
import com.passioagogo.market.pdf.generator.PdfGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PdfModule {

    @Provides
    @Singleton
    fun providePdfGenerator(
        @ApplicationContext context: Context
    ): PdfGenerator {
        return PdfGenerator(context)
    }

    @Provides
    @Singleton
    fun provideCatalogoRepository(

    ): CatalogoRepository {
        return CatalogoRepositoryImpl(
            productoDao = TODO(),
            familiaDao = TODO(),
            categoriaDao = TODO(),
            subcategoriaDao = TODO(),
            atributoDao = TODO()
        )
    }
}