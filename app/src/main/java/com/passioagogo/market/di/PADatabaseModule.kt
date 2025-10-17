package com.passioagogo.market.di

import android.content.Context
import com.passioagogo.market.data.local.InventarioDatabase
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.dao.HistorialPrecioDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoFamiliaDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.data.local.dao.TipoAtributoDao
import com.passioagogo.market.data.local.dao.VentaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PADatabaseModule {

    @Singleton
    @Provides
    fun provideInventarioDatabase(
        @ApplicationContext context: Context,
    ): InventarioDatabase {
        return InventarioDatabase.getDatabase(context)
    }


    @Singleton
    @Provides
    fun provideFamiliaDao(
        db: InventarioDatabase
    ): FamiliaDao {
        return db.familiaDao()
    }

    @Singleton
    @Provides
    fun provideCategoriaDao(
        db: InventarioDatabase
    ): CategoriaDao {
        return db.categoriaDao()
    }

    @Singleton
    @Provides
    fun provideSubCategoriaDao(
        db: InventarioDatabase
    ): SubcategoriaDao {
        return db.subcategoriaDao()
    }

    @Singleton
    @Provides
    fun provideProveedorDao(
        db: InventarioDatabase
    ): ProveedorDao {
        return db.proveedorDao()
    }

    @Singleton
    @Provides
    fun provideProductoDao(
        db: InventarioDatabase
    ): ProductoDao {
        return db.productoDao()
    }

    @Singleton
    @Provides
    fun provideProTOCatDao(
        db: InventarioDatabase
    ): ProductoCategoriaDao {
        return db.productoCategoriaDao()
    }

    @Singleton
    @Provides
    fun provideProTOFamDao(
        db: InventarioDatabase
    ): ProductoFamiliaDao{
        return db.productoFamiliaDao()
    }

    @Singleton
    @Provides
    fun provideProTOSubCatDao(
        db: InventarioDatabase
    ): ProductoSubcategoriaDao {
        return db.productoSubcategoriaDao()
    }

    @Singleton
    @Provides
    fun provideProTOProveedorDao(
        db: InventarioDatabase
    ): ProductoProveedorDao {
        return db.productoProveedorDao()
    }

    @Singleton
    @Provides
    fun provideTipoAtributoDao(
        db: InventarioDatabase
    ): TipoAtributoDao {
        return db.tipoAtributoDao()
    }

    @Singleton
    @Provides
    fun provideProductoAtributoDao(
        db: InventarioDatabase
    ): ProductoAtributoDao {
        return db.productoAtributoDao()
    }

    @Singleton
    @Provides
    fun provideProductoImagenDao(
        db: InventarioDatabase
    ): ProductoImagenDao {
        return db.productoImagenDao()
    }

    @Singleton
    @Provides
    fun provideHistorialPrecioDao(
        db: InventarioDatabase
    ): HistorialPrecioDao {
        return db.historialPrecioDao()
    }

    @Singleton
    @Provides
    fun provideVentaDao(
        db: InventarioDatabase
    ): VentaDao {
        return db.ventaDao()
    }
}