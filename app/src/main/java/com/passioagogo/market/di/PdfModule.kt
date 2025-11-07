package com.passioagogo.market.di

import android.content.Context
import com.passioagogo.market.data.implementation.CatalogoRepositoryImpl
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
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
        productoDao: ProductoDao,
        familiaDao: FamiliaDao,
        categoriaDao: CategoriaDao,
        subcategoriaDao: SubcategoriaDao,
        productoAtributoDao: ProductoAtributoDao
    ): CatalogoRepository {
        return CatalogoRepositoryImpl(
            productoDao = productoDao,
            familiaDao = familiaDao,
            categoriaDao = categoriaDao,
            subcategoriaDao = subcategoriaDao,
            atributoDao = productoAtributoDao
        )
    }
}