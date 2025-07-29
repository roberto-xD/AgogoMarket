package com.passioagogo.market.injection

import com.passioagogo.market.data.repository.CategoriaRepository
import com.passioagogo.market.data.repository.CategoriaRepositoryImpl
import com.passioagogo.market.data.repository.FamiliaRepository
import com.passioagogo.market.data.repository.FamiliaRepositoryImpl
import com.passioagogo.market.data.repository.ProductoRepository
import com.passioagogo.market.data.repository.ProductoRepositoryImpl
import com.passioagogo.market.data.repository.ProveedorRepository
import com.passioagogo.market.data.repository.ProveedorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProductoRepository(
        productoRepositoryImpl: ProductoRepositoryImpl
    ): ProductoRepository

    @Binds
    abstract fun bindCategoriaRepository(
        categoriaRepositoryImpl: CategoriaRepositoryImpl
    ): CategoriaRepository

    @Binds
    abstract fun bindProveedorRepository(
        proveedorRepositoryImpl: ProveedorRepositoryImpl
    ): ProveedorRepository

    @Binds
    abstract fun bindFamiliaRepository(
        familiaRepositoryImpl: FamiliaRepositoryImpl
    ): FamiliaRepository
}