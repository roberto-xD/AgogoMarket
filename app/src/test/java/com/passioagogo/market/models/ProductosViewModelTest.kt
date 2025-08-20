package com.passioagogo.market.models

import com.google.common.truth.Truth
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.producto.BuscarProductosParams
import com.passioagogo.market.domain.usecase.producto.BuscarProductosUseCase
import com.passioagogo.market.domain.usecase.producto.CrearProductoUseCase
import com.passioagogo.market.domain.usecase.producto.EliminarProductoUseCase
import com.passioagogo.market.domain.usecase.categorias.ObtenerCategoriasUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosStockBajoUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProductosUseCase
import com.passioagogo.market.domain.usecase.producto.ObtenerProveedoresUseCase
import com.passioagogo.market.presentation.viewModel.products.ProductosViewModel
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.test
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
class ProductosViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    // Mocks de casos de uso
    private val mockObtenerProductosUseCase: ObtenerProductosUseCase = mockk()
    private val mockObtenerProductosStockBajoUseCase: ObtenerProductosStockBajoUseCase = mockk()
    private val mockBuscarProductosUseCase: BuscarProductosUseCase = mockk()
    private val mockCrearProductoUseCase: CrearProductoUseCase = mockk()
    private val mockEliminarProductoUseCase: EliminarProductoUseCase = mockk()
    private val mockObtenerCategoriasUseCase: ObtenerCategoriasUseCase = mockk()
    private val mockObtenerProveedoresUseCase: ObtenerProveedoresUseCase = mockk()

    private lateinit var viewModel: ProductosViewModel

    @BeforeEach
    fun setup() {
        clearAllMocks()
        setupDefaultMocks()
        viewModel = ProductosViewModel(
            obtenerProductosUseCase = mockObtenerProductosUseCase,
            obtenerProductosStockBajoUseCase = mockObtenerProductosStockBajoUseCase,
            buscarProductosUseCase = mockBuscarProductosUseCase,
            crearProductoUseCase = mockCrearProductoUseCase,
            eliminarProductoUseCase = mockEliminarProductoUseCase,
            obtenerCategoriasUseCase = mockObtenerCategoriasUseCase,
            obtenerProveedoresUseCase = mockObtenerProveedoresUseCase
        )
    }

    private fun setupDefaultMocks() {
        // Configurar mocks para que los casos de uso retornen flows vacíos por defecto
        coEvery { mockObtenerProductosUseCase() } returns PADomainState.Success(flowOf(emptyList()))
        coEvery { mockObtenerProductosStockBajoUseCase() } returns PADomainState.Success(flowOf(emptyList()))
        coEvery { mockObtenerCategoriasUseCase() } returns PADomainState.Success(flowOf(emptyList()))
        coEvery { mockObtenerProveedoresUseCase() } returns PADomainState.Success(flowOf(emptyList()))
    }

    @Test
    fun `init debe cargar datos iniciales correctamente`() = runTest {
        // Given
        val productos = listOf(TestData.productoTest1, TestData.productoTest2)
        val productosStockBajo = listOf(TestData.productoTest2)
        val categorias = listOf(TestData.categoriaTest1)
        val proveedores = listOf(TestData.proveedorTest1)

        coEvery { mockObtenerProductosUseCase() } returns PADomainState.Success(flowOf(productos))
        coEvery { mockObtenerProductosStockBajoUseCase() } returns PADomainState.Success(flowOf(productosStockBajo))
        coEvery { mockObtenerCategoriasUseCase() } returns PADomainState.Success(flowOf(categorias))
        coEvery { mockObtenerProveedoresUseCase() } returns PADomainState.Success(flowOf(proveedores))

        // When
        viewModel = ProductosViewModel(
            obtenerProductosUseCase = mockObtenerProductosUseCase,
            obtenerProductosStockBajoUseCase = mockObtenerProductosStockBajoUseCase,
            buscarProductosUseCase = mockBuscarProductosUseCase,
            crearProductoUseCase = mockCrearProductoUseCase,
            eliminarProductoUseCase = mockEliminarProductoUseCase,
            obtenerCategoriasUseCase = mockObtenerCategoriasUseCase,
            obtenerProveedoresUseCase = mockObtenerProveedoresUseCase
        )

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.productos).hasSize(2)
            Truth.assertThat(state.productosStockBajo).hasSize(1)
            Truth.assertThat(state.categorias).hasSize(1)
            Truth.assertThat(state.proveedores).hasSize(1)
            Truth.assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun `buscarProductos debe actualizar lista con resultados de busqueda`() = runTest {
        // Given
        val query = "TEST"
        val productosEncontrados = listOf(TestData.productoTest1)
        val params = BuscarProductosParams(query = query)

        coEvery { mockBuscarProductosUseCase(params) } returns PADomainState.Success(flowOf(productosEncontrados))

        // When
        viewModel.buscarProductos(query)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.productos).hasSize(1)
            Truth.assertThat(state.productos[0]).isEqualTo(TestData.productoTest1)
            Truth.assertThat(state.isLoading).isFalse()
        }

        coVerify { mockBuscarProductosUseCase(params) }
    }

    @Test
    fun `buscarProductos con query vacio debe recargar todos los productos`() = runTest {
        // Given
        val query = ""

        // When
        viewModel.buscarProductos(query)

        // Then
        // Debe llamar a cargarDatosIniciales en lugar de buscar
        coVerify(exactly = 0) { mockBuscarProductosUseCase(any()) }
        coVerify(atLeast = 1) { mockObtenerProductosUseCase() }
    }

    @Test
    fun `crearProducto con datos validos debe mostrar mensaje de exito`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val nuevoId = 1L

        coEvery { mockCrearProductoUseCase(params) } returns PADomainState.Success(nuevoId)

        // When
        viewModel.crearProducto(params)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.mensajeExito).isEqualTo("Producto creado exitosamente")
            Truth.assertThat(state.isLoading).isFalse()
            Truth.assertThat(state.errorMessage).isNull()
        }

        coVerify { mockCrearProductoUseCase(params) }
    }

    @Test
    fun `crearProducto con sku duplicado debe mostrar error especifico`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val error = DomainException.SkuDuplicado(params.skuInterno)

        coEvery { mockCrearProductoUseCase(params) } returns PADomainState.Error(error)

        // When
        viewModel.crearProducto(params)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.errorMessage).isEqualTo("El SKU ya existe")
            Truth.assertThat(state.isLoading).isFalse()
            Truth.assertThat(state.mensajeExito).isNull()
        }
    }

    @Test
    fun `crearProducto con codigo barras duplicado debe mostrar error especifico`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val error = DomainException.CodigoBarrasDuplicado(params.codigoBarras!!)

        coEvery { mockCrearProductoUseCase(params) } returns PADomainState.Error(error)

        // When
        viewModel.crearProducto(params)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.errorMessage).isEqualTo("El código de barras ya existe")
            Truth.assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun `eliminarProducto debe mostrar mensaje de exito`() = runTest {
        // Given
        val productoId = 1L

        coEvery { mockEliminarProductoUseCase(productoId) } returns PADomainState.Success(Unit)

        // When
        viewModel.eliminarProducto(productoId)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.mensajeExito).isEqualTo("Producto eliminado exitosamente")
            Truth.assertThat(state.errorMessage).isNull()
        }

        coVerify { mockEliminarProductoUseCase(productoId) }
    }

    @Test
    fun `eliminarProducto con error debe mostrar mensaje de error`() = runTest {
        // Given
        val productoId = 1L
        val error = RuntimeException("Error de base de datos")

        coEvery { mockEliminarProductoUseCase(productoId) } returns PADomainState.Error(error)

        // When
        viewModel.eliminarProducto(productoId)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.errorMessage).isEqualTo("Error de base de datos")
            Truth.assertThat(state.mensajeExito).isNull()
        }
    }

    @Test
    fun `limpiarMensajes debe resetear mensajes de error y exito`() = runTest {
        // Given - Configurar estado inicial con mensajes
        coEvery { mockCrearProductoUseCase(any()) } returns PADomainState.Success(1L)
        viewModel.crearProducto(TestData.crearProductoParamsValidos)

        // When
        viewModel.limpiarMensajes()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.errorMessage).isNull()
            Truth.assertThat(state.mensajeExito).isNull()
        }
    }

    @Test
    fun `filtrarPorCategoria debe actualizar categoria seleccionada`() = runTest {
        // Given
        val categoriaId = 1L

        // When
        viewModel.filtrarPorCategoria(categoriaId)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.categoriaSeleccionada).isEqualTo(categoriaId)
        }
    }
}