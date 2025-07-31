package com.passioagogo.market.injection

import com.passioagogo.market.data.repository.CategoriaRepositoryImpl
import com.passioagogo.market.data.repository.FamiliaRepositoryImpl
import com.passioagogo.market.data.repository.ProductoRepositoryImpl
import com.passioagogo.market.data.repository.ProveedorRepositoryImpl
import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.repository.IFamiliaRepository
import com.passioagogo.market.domain.repository.IHistorialRepository
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.usecase.ActualizarProductoUseCase
import com.passioagogo.market.domain.usecase.BuscarProductosUseCase
import com.passioagogo.market.domain.usecase.CrearCategoriaUseCase
import com.passioagogo.market.domain.usecase.CrearProductoUseCase
import com.passioagogo.market.domain.usecase.CrearProveedorUseCase
import com.passioagogo.market.domain.usecase.EliminarProductoUseCase
import com.passioagogo.market.domain.usecase.EstablecerImagenPrincipalUseCase
import com.passioagogo.market.domain.usecase.GenerarReporteVentasUseCase
import com.passioagogo.market.domain.usecase.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.GuardarImagenProductoUseCase
import com.passioagogo.market.domain.usecase.ObtenerCategoriasPorFamiliaUseCase
import com.passioagogo.market.domain.usecase.ObtenerCategoriasUseCase
import com.passioagogo.market.domain.usecase.ObtenerMetricasInventarioUseCase
import com.passioagogo.market.domain.usecase.ObtenerProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.ObtenerProductoPorIdUseCase
import com.passioagogo.market.domain.usecase.ObtenerProductosStockBajoUseCase
import com.passioagogo.market.domain.usecase.ObtenerProductosUseCase
import com.passioagogo.market.domain.usecase.ObtenerProveedoresUseCase
import com.passioagogo.market.domain.usecase.RegistrarCompraUseCase
import com.passioagogo.market.domain.usecase.RegistrarVentaUseCase
import com.passioagogo.market.domain.usecase.ValidarCodigoBarrasUseCase
import com.passioagogo.market.domain.usecase.ValidarSkuUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // ===========================================
    // CASOS DE USO - PRODUCTOS
    // ===========================================

    @Provides
    fun provideObtenerProductosUseCase(
        productoRepository: IProductoRepository
    ): ObtenerProductosUseCase = ObtenerProductosUseCase(productoRepository)

    @Provides
    fun provideObtenerProductoPorIdUseCase(
        productoRepository: IProductoRepository
    ): ObtenerProductoPorIdUseCase = ObtenerProductoPorIdUseCase(productoRepository)

    @Provides
    fun provideObtenerProductoDetalladoUseCase(
        productoRepository: IProductoRepository
    ): ObtenerProductoDetalladoUseCase = ObtenerProductoDetalladoUseCase(productoRepository)

    @Provides
    fun provideBuscarProductosUseCase(
        productoRepository: IProductoRepository
    ): BuscarProductosUseCase = BuscarProductosUseCase(productoRepository)

    @Provides
    fun provideCrearProductoUseCase(
        productoRepository: IProductoRepository
    ): CrearProductoUseCase = CrearProductoUseCase(productoRepository)

    @Provides
    fun provideActualizarProductoUseCase(
        productoRepository: IProductoRepository,
        historialRepository: IHistorialRepository
    ): ActualizarProductoUseCase = ActualizarProductoUseCase(productoRepository, historialRepository)

    @Provides
    fun provideGestionarStockUseCase(
        productoRepository: IProductoRepository
    ): GestionarStockUseCase = GestionarStockUseCase(productoRepository)

    @Provides
    fun provideRegistrarVentaUseCase(
        productoRepository: IProductoRepository
    ): RegistrarVentaUseCase = RegistrarVentaUseCase(productoRepository)

    @Provides
    fun provideRegistrarCompraUseCase(
        productoRepository: IProductoRepository
    ): RegistrarCompraUseCase = RegistrarCompraUseCase(productoRepository)

    @Provides
    fun provideObtenerProductosStockBajoUseCase(
        productoRepository: IProductoRepository
    ): ObtenerProductosStockBajoUseCase = ObtenerProductosStockBajoUseCase(productoRepository)

    @Provides
    fun provideEliminarProductoUseCase(
        productoRepository: IProductoRepository
    ): EliminarProductoUseCase = EliminarProductoUseCase(productoRepository)

    // ===========================================
    // CASOS DE USO - CATEGORÍAS
    // ===========================================

    @Provides
    fun provideObtenerCategoriasUseCase(
        categoriaRepository: ICategoriaRepository
    ): ObtenerCategoriasUseCase = ObtenerCategoriasUseCase(categoriaRepository)

    @Provides
    fun provideObtenerCategoriasPorFamiliaUseCase(
        categoriaRepository: ICategoriaRepository
    ): ObtenerCategoriasPorFamiliaUseCase = ObtenerCategoriasPorFamiliaUseCase(categoriaRepository)

    @Provides
    fun provideCrearCategoriaUseCase(
        categoriaRepository: ICategoriaRepository
    ): CrearCategoriaUseCase = CrearCategoriaUseCase(categoriaRepository)

    // ===========================================
    // CASOS DE USO - PROVEEDORES
    // ===========================================

    @Provides
    fun provideObtenerProveedoresUseCase(
        proveedorRepository: IProveedorRepository
    ): ObtenerProveedoresUseCase = ObtenerProveedoresUseCase(proveedorRepository)

    @Provides
    fun provideCrearProveedorUseCase(
        proveedorRepository: IProveedorRepository
    ): CrearProveedorUseCase = CrearProveedorUseCase(proveedorRepository)

    // ===========================================
    // CASOS DE USO - MÉTRICAS Y REPORTES
    // ===========================================

    @Provides
    fun provideObtenerMetricasInventarioUseCase(
        productoRepository: IProductoRepository
    ): ObtenerMetricasInventarioUseCase = ObtenerMetricasInventarioUseCase(productoRepository)

    @Provides
    fun provideGenerarReporteVentasUseCase(
        productoRepository: IProductoRepository,
        categoriaRepository: ICategoriaRepository
    ): GenerarReporteVentasUseCase = GenerarReporteVentasUseCase(productoRepository, categoriaRepository)

    // ===========================================
    // CASOS DE USO - VALIDACIONES
    // ===========================================

    @Provides
    fun provideValidarCodigoBarrasUseCase(
        productoRepository: IProductoRepository
    ): ValidarCodigoBarrasUseCase = ValidarCodigoBarrasUseCase(productoRepository)

    @Provides
    fun provideValidarSkuUseCase(
        productoRepository: IProductoRepository
    ): ValidarSkuUseCase = ValidarSkuUseCase(productoRepository)

    // ===========================================
    // CASOS DE USO - IMÁGENES
    // ===========================================

    @Provides
    fun provideGuardarImagenProductoUseCase(
        imagenRepository: IImagenRepository
    ): GuardarImagenProductoUseCase = GuardarImagenProductoUseCase(imagenRepository)

    @Provides
    fun provideEstablecerImagenPrincipalUseCase(
        imagenRepository: IImagenRepository
    ): EstablecerImagenPrincipalUseCase = EstablecerImagenPrincipalUseCase(imagenRepository)
}

// ===========================================
// BINDING DE INTERFACES DE REPOSITORY
// ===========================================

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainRepositoryModule {

    @Binds
    abstract fun bindIProductoRepository(
        productoRepositoryImpl: ProductoRepositoryImpl
    ): IProductoRepository

    @Binds
    abstract fun bindICategoriaRepository(
        categoriaRepositoryImpl: CategoriaRepositoryImpl
    ): ICategoriaRepository

    @Binds
    abstract fun bindIProveedorRepository(
        proveedorRepositoryImpl: ProveedorRepositoryImpl
    ): IProveedorRepository

    @Binds
    abstract fun bindIFamiliaRepository(
        familiaRepositoryImpl: FamiliaRepositoryImpl
    ): IFamiliaRepository

    // Nota: IImagenRepository e IHistorialRepository necesitan implementaciones
    // que puedes crear siguiendo el mismo patrón de los repositorios existentes
}