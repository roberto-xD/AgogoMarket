package com.passioagogo.market.di

import com.google.gson.Gson
import com.passioagogo.market.data.repository.CategoriaRepositoryDomainImpl
import com.passioagogo.market.data.repository.FamiliaRepositoryDomainImpl
import com.passioagogo.market.data.repository.HistorialRepositoryImpl
import com.passioagogo.market.data.repository.ImagenRepositoryImpl
import com.passioagogo.market.data.repository.ProductoRepositoryDomainImpl
import com.passioagogo.market.data.repository.ProveedorRepositoryDomainImpl
import com.passioagogo.market.data.repository.SubcategoriaRepositoryDomainImpl
import com.passioagogo.market.data.repository.VentaRepositoryImpl
import com.passioagogo.market.data.imports.GoogleSheetsImportService
import com.passioagogo.market.data.imports.GoogleSheetsImportServiceImpl
import com.passioagogo.market.domain.repository.ICategoriaRepository
import com.passioagogo.market.domain.repository.VentaRepository
import com.passioagogo.market.domain.repository.FamiliaRepository
import com.passioagogo.market.domain.repository.IHistorialRepository
import com.passioagogo.market.domain.repository.IImagenRepository
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.repository.IProveedorRepository
import com.passioagogo.market.domain.repository.ISubcategoriaRepository
import com.passioagogo.market.domain.usecase.categorias.CrearCategoriaConFamiliaUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasPorFamiliaIdUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasUseCase
import com.passioagogo.market.domain.usecase.compras.CrearProveedorUseCase
import com.passioagogo.market.domain.usecase.compras.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.compras.RegistrarCompraUseCase
import com.passioagogo.market.domain.usecase.familias.CrearFamiliaUseCase
import com.passioagogo.market.domain.usecase.familias.ObtenerFamiliasUseCase
import com.passioagogo.market.domain.usecase.metricas.ObtenerMetricasInventarioUseCase
import com.passioagogo.market.domain.usecase.producto.ActualizarProductoUseCase
import com.passioagogo.market.domain.usecase.producto.BuscarProductosUseCase
import com.passioagogo.market.domain.usecase.producto.CrearProductoUseCase
import com.passioagogo.market.domain.usecase.producto.EliminarProductoUseCase
import com.passioagogo.market.domain.usecase.producto.EstablecerImagenPrincipalUseCase
import com.passioagogo.market.domain.usecase.producto.GuardarImagenProductoUseCase
import com.passioagogo.market.domain.usecase.producto.ImportarProductosDesdeGoogleSheetsUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductoPorIdUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosStockBajoUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProveedoresUseCase
import com.passioagogo.market.domain.usecase.producto.ValidarCodigoBarrasUseCase
import com.passioagogo.market.domain.usecase.producto.ValidarSkuUseCase
import com.passioagogo.market.domain.usecase.subcategorias.CrearSubcategoriaConCategoriaUseCase
import com.passioagogo.market.domain.usecase.subcategorias.ObtenerSubcategoriaPorCategoriaUseCase
import com.passioagogo.market.domain.usecase.ventas.GenerarReporteVentasUseCase
import com.passioagogo.market.domain.usecase.ventas.RegistrarVentaUseCase
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
    fun provideObtenerFamiliasUseCase(
        familiaRepository: FamiliaRepository
    ): ObtenerFamiliasUseCase = ObtenerFamiliasUseCase(familiaRepository)
    @Provides
    fun provideObtenerCategoriasUseCase(
        categoriaRepository: ICategoriaRepository
    ): ObtenerCategoriasUseCase = ObtenerCategoriasUseCase(categoriaRepository)

    @Provides
    fun provideObtenerCategoriasPorFamiliaUseCase(
        categoriaRepository: ICategoriaRepository
    ): ObtenerCategoriasPorFamiliaIdUseCase = ObtenerCategoriasPorFamiliaIdUseCase(categoriaRepository)

    @Provides
    fun provideCrearCategoriaConFamiliaUseCase(
        categoriaRepository: ICategoriaRepository
    ): CrearCategoriaConFamiliaUseCase = CrearCategoriaConFamiliaUseCase(categoriaRepository)

    @Provides
    fun provideCrearFamiliaUseCase(
        familiaRepository: FamiliaRepository
    ): CrearFamiliaUseCase = CrearFamiliaUseCase(familiaRepository)

    @Provides
    fun provideCrearSubcategoriaConCategoriaUseCase(
        subcategoriaRepository: ISubcategoriaRepository
    ): CrearSubcategoriaConCategoriaUseCase = CrearSubcategoriaConCategoriaUseCase(subcategoriaRepository)

    @Provides
    fun provideObtenerSubcategoriaPorCategoriaUseCase(
        subcategoriaRepository: ISubcategoriaRepository
    ): ObtenerSubcategoriaPorCategoriaUseCase = ObtenerSubcategoriaPorCategoriaUseCase(subcategoriaRepository)
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

    @Provides
    fun provideImportarProductosDesdeGoogleSheetsUseCase(
        googleSheetsImportServiceImpl : GoogleSheetsImportService,
        crearProductoUseCase: CrearProductoUseCase,
        crearFamiliaUseCase: CrearFamiliaUseCase,
        crearCategoriaUseCase: CrearCategoriaConFamiliaUseCase,
        crearSubcategoriaUseCase: CrearSubcategoriaConCategoriaUseCase,
        productoRepository: IProductoRepository,
        familiaRepository: FamiliaRepository,
        categoriaRepository: ICategoriaRepository,
        subcategoriaRepository: ISubcategoriaRepository,
    ) : ImportarProductosDesdeGoogleSheetsUseCase = ImportarProductosDesdeGoogleSheetsUseCase(
        googleSheetsService = googleSheetsImportServiceImpl,
        crearProductoUseCase = crearProductoUseCase,
        crearFamiliaUseCase = crearFamiliaUseCase,
        crearCategoriaUseCase = crearCategoriaUseCase,
        crearSubcategoriaUseCase = crearSubcategoriaUseCase,
        productoRepository = productoRepository,
        familiaRepository = familiaRepository,
        categoriaRepository = categoriaRepository,
        subcategoriaRepository = subcategoriaRepository,
    )

    @Provides
    fun provideGoogleSheetsImportService() : GoogleSheetsImportService =
        GoogleSheetsImportServiceImpl(
            httpClient = provideOkHttpClient(),
            gson = Gson()
        )
}

// ===========================================
// BINDING DE INTERFACES DE REPOSITORY
// ===========================================

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainRepositoryModule {

    @Binds
    abstract fun bindProductoRepository(
        productoRepositoryDomainImpl: ProductoRepositoryDomainImpl
    ): IProductoRepository

    @Binds
    abstract fun bindICategoriaRepository(
        categoriaRepositoryDomainImpl: CategoriaRepositoryDomainImpl
    ): ICategoriaRepository

    @Binds
    abstract fun bindIFamiliaRepository(
        familiaRepositoryDomainImpl: FamiliaRepositoryDomainImpl
    ): FamiliaRepository

    @Binds
    abstract fun bindSubcategoriaRepository(
        subcategoriaRepositoryDomainImpl: SubcategoriaRepositoryDomainImpl
    ): ISubcategoriaRepository

    @Binds
    abstract fun bindProveedorRepository(
        proveedorRepositoryDomainImpl: ProveedorRepositoryDomainImpl
    ): IProveedorRepository

    @Binds
    abstract fun bindHistorialRepository(
        historialRepositoryDomainImpl: HistorialRepositoryImpl
    ): IHistorialRepository

    @Binds
    abstract fun bindImagenRepository(
        imagenRepositoryDomainImpl: ImagenRepositoryImpl
    ): IImagenRepository

    @Binds
    abstract fun bindVentaRepository(
        impl: VentaRepositoryImpl
    ): VentaRepository
}