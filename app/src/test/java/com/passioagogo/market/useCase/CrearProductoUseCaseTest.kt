package com.passioagogo.market.useCase

import com.google.common.truth.Truth
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.producto.CrearProductoUseCase
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.shouldBeError
import com.passioagogo.market.testing.shouldBeSuccess
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.jvm.java

@ExperimentalCoroutinesApi
class CrearProductoUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockProductoRepository: IProductoRepository = mockk()
    private val crearProductoUseCase = CrearProductoUseCase(mockProductoRepository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `crear producto con datos validos debe retornar exito`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val expectedId = 1L

        coEvery { mockProductoRepository.obtenerProductoPorSku(params.skuInterno) } returns null
        coEvery { mockProductoRepository.obtenerProductoPorCodigoBarras(any()) } returns null
        coEvery { mockProductoRepository.guardarProducto(any()) } returns PADomainState.Success(expectedId)

        // When
        val result = crearProductoUseCase(params)

        // Then
        val productId = result.shouldBeSuccess()
        Truth.assertThat(productId).isEqualTo(expectedId)

        coVerify { mockProductoRepository.obtenerProductoPorSku(params.skuInterno) }
        coVerify { mockProductoRepository.obtenerProductoPorCodigoBarras(params.codigoBarras!!) }
        coVerify { mockProductoRepository.guardarProducto(any()) }
    }

    @Test
    fun `crear producto con nombre vacio debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos.copy(nombre = "")

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
        Truth.assertThat(error.message).contains("nombre del producto no puede estar vacío")

        coVerify(exactly = 0) { mockProductoRepository.guardarProducto(any()) }
    }

    @Test
    fun `crear producto con sku vacio debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos.copy(skuInterno = "")

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
        Truth.assertThat(error.message).contains("SKU interno no puede estar vacío")
    }

    @Test
    fun `crear producto con precio compra negativo debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos.copy(precioCompra = -10.0)

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.PrecioInvalido::class.java)
    }

    @Test
    fun `crear producto con sku duplicado debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val productoExistente = TestData.productoTest1.copy(skuInterno = params.skuInterno)

        coEvery { mockProductoRepository.obtenerProductoPorSku(params.skuInterno) } returns productoExistente

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.SkuDuplicado::class.java)
        Truth.assertThat(error.message).contains(params.skuInterno)

        coVerify(exactly = 0) { mockProductoRepository.guardarProducto(any()) }
    }

    @Test
    fun `crear producto con codigo barras duplicado debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val productoExistente = TestData.productoTest1.copy(codigoBarras = params.codigoBarras)

        coEvery { mockProductoRepository.obtenerProductoPorSku(params.skuInterno) } returns null
        coEvery { mockProductoRepository.obtenerProductoPorCodigoBarras(params.codigoBarras!!) } returns productoExistente

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.CodigoBarrasDuplicado::class.java)
        Truth.assertThat(error.message).contains(params.codigoBarras)
    }

    @Test
    fun `crear producto cuando repository falla debe retornar error`() = runTest {
        // Given
        val params = TestData.crearProductoParamsValidos
        val expectedException = RuntimeException("Database error")

        coEvery { mockProductoRepository.obtenerProductoPorSku(any()) } returns null
        coEvery { mockProductoRepository.obtenerProductoPorCodigoBarras(any()) } returns null
        coEvery { mockProductoRepository.guardarProducto(any()) } returns PADomainState.Error(expectedException)

        // When
        val result = crearProductoUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isEqualTo(expectedException)
    }
}