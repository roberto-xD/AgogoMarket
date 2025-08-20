package com.passioagogo.market.models

import com.google.common.truth.Truth
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.producto.ActualizarProductoUseCase
import com.passioagogo.market.domain.usecase.compras.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.producto.GuardarImagenProductoUseCase
import com.passioagogo.market.domain.usecase.compras.MovimientoStockParams
import com.passioagogo.market.domain.usecase.producto.ObtenerProductoDetalladoUseCase
import com.passioagogo.market.domain.usecase.compras.RegistrarCompraUseCase
import com.passioagogo.market.domain.usecase.ventas.RegistrarVentaParams
import com.passioagogo.market.domain.usecase.ventas.RegistrarVentaUseCase
import com.passioagogo.market.domain.usecase.compras.TipoMovimiento
import com.passioagogo.market.domain.usecase.producto.ValidarCodigoBarrasUseCase
import com.passioagogo.market.domain.usecase.producto.ValidarSkuParams
import com.passioagogo.market.domain.usecase.producto.ValidarSkuUseCase
import com.passioagogo.market.presentation.viewModel.products.DetalleProductoViewModel
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.test
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
class DetalleProductoViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockObtenerProductoDetalladoUseCase: ObtenerProductoDetalladoUseCase = mockk()
    private val mockActualizarProductoUseCase: ActualizarProductoUseCase = mockk()
    private val mockGestionarStockUseCase: GestionarStockUseCase = mockk()
    private val mockRegistrarVentaUseCase: RegistrarVentaUseCase = mockk()
    private val mockRegistrarCompraUseCase: RegistrarCompraUseCase = mockk()
    private val mockGuardarImagenProductoUseCase: GuardarImagenProductoUseCase = mockk()
    private val mockValidarSkuUseCase: ValidarSkuUseCase = mockk()
    private val mockValidarCodigoBarrasUseCase: ValidarCodigoBarrasUseCase = mockk()

    private lateinit var viewModel: DetalleProductoViewModel

    @BeforeEach
    fun setup() {
        clearAllMocks()
        viewModel = DetalleProductoViewModel(
            obtenerProductoDetalladoUseCase = mockObtenerProductoDetalladoUseCase,
            actualizarProductoUseCase = mockActualizarProductoUseCase,
            gestionarStockUseCase = mockGestionarStockUseCase,
            registrarVentaUseCase = mockRegistrarVentaUseCase,
            registrarCompraUseCase = mockRegistrarCompraUseCase,
            guardarImagenProductoUseCase = mockGuardarImagenProductoUseCase,
            validarSkuUseCase = mockValidarSkuUseCase,
            validarCodigoBarrasUseCase = mockValidarCodigoBarrasUseCase
        )
    }

    @Test
    fun `cargarProducto debe actualizar estado con producto detallado`() = runTest {
        // Given
        val productoId = 1L
        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            categorias = listOf(TestData.categoriaTest1),
            proveedores = listOf(ProveedorConPrecio(TestData.proveedorTest1, 15.0, null))
        )

        coEvery { mockObtenerProductoDetalladoUseCase(productoId) } returns PADomainState.Success(productoDetallado)

        // When
        viewModel.cargarProducto(productoId)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.productoDetallado).isEqualTo(productoDetallado)
            Truth.assertThat(state.isLoading).isFalse()
            Truth.assertThat(state.errorMessage).isNull()
        }

        coVerify { mockObtenerProductoDetalladoUseCase(productoId) }
    }

    @Test
    fun `actualizarStock debe gestionar stock y recargar producto`() = runTest {
        // Given
        val productoId = 1L
        val nuevaCantidad = 20
        val tipoMovimiento = TipoMovimiento.ENTRADA
        val motivo = "Compra"
        val productoDetallado = ProductoDetallado(producto = TestData.productoTest1)

        // Configurar estado inicial
        coEvery { mockObtenerProductoDetalladoUseCase(productoId) } returns PADomainState.Success(productoDetallado)
        viewModel.cargarProducto(productoId)

        val params = MovimientoStockParams(productoId, nuevaCantidad, tipoMovimiento, motivo)
        coEvery { mockGestionarStockUseCase(params) } returns PADomainState.Success(Unit)

        // When
        viewModel.actualizarStock(nuevaCantidad, tipoMovimiento, motivo)

        // Then
        coVerify { mockGestionarStockUseCase(params) }
        coVerify(atLeast = 2) { mockObtenerProductoDetalladoUseCase(productoId) } // Una para cargar, otra para recargar
    }

    @Test
    fun `registrarVenta debe procesar venta y mostrar mensaje de exito`() = runTest {
        // Given
        val productoId = 1L
        val cantidad = 5
        val productoDetallado = ProductoDetallado(producto = TestData.productoTest1)

        // Configurar estado inicial
        coEvery { mockObtenerProductoDetalladoUseCase(productoId) } returns PADomainState.Success(productoDetallado)
        viewModel.cargarProducto(productoId)

        val params = RegistrarVentaParams(productoId, cantidad)
        coEvery { mockRegistrarVentaUseCase(params) } returns PADomainState.Success(Unit)

        // When
        viewModel.registrarVenta(cantidad)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.mensajeExito).isEqualTo("Venta registrada exitosamente")
        }

        coVerify { mockRegistrarVentaUseCase(params) }
    }

    @Test
    fun `validarSku debe actualizar estado de validacion`() = runTest {
        // Given
        val sku = "TEST001"
        val productoId = 1L
        val productoDetallado = ProductoDetallado(producto = TestData.productoTest1.copy(id = productoId))

        // Configurar estado inicial
        coEvery { mockObtenerProductoDetalladoUseCase(productoId) } returns PADomainState.Success(productoDetallado)
        viewModel.cargarProducto(productoId)

        val params = ValidarSkuParams(sku, productoId)
        coEvery { mockValidarSkuUseCase(params) } returns PADomainState.Success(true)

        // When
        viewModel.validarSku(sku)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.skuValido).isTrue()
        }

        coVerify { mockValidarSkuUseCase(params) }
    }

    @Test
    fun `validarCodigoBarras debe actualizar estado de validacion`() = runTest {
        // Given
        val codigo = "1234567890123"

        coEvery { mockValidarCodigoBarrasUseCase(codigo) } returns PADomainState.Success(true)

        // When
        viewModel.validarCodigoBarras(codigo)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            Truth.assertThat(state.codigoBarrasValido).isTrue()
        }

        coVerify { mockValidarCodigoBarrasUseCase(codigo) }
    }
}