package com.passioagogo.market.useCase

import com.google.common.truth.Truth
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.GestionarStockUseCase
import com.passioagogo.market.domain.usecase.MovimientoStockParams
import com.passioagogo.market.domain.usecase.TipoMovimiento
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.shouldBeError
import com.passioagogo.market.testing.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GestionarStockUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockProductoRepository: IProductoRepository = mockk()
    private val gestionarStockUseCase = GestionarStockUseCase(mockProductoRepository)

    @Test
    fun `aumentar stock debe sumar cantidad correctamente`() = runTest {
        // Given
        val producto = TestData.productoTest1.copy(cantidadActual = 50)
        val params = MovimientoStockParams(
            productoId = producto.id,
            cantidad = 20,
            tipoMovimiento = TipoMovimiento.ENTRADA,
            motivo = "Compra"
        )
        val cantidadEsperada = 70

        coEvery { mockProductoRepository.obtenerProductoPorId(producto.id) } returns producto
        coEvery { mockProductoRepository.actualizarStock(producto.id, cantidadEsperada) } returns PADomainState.Success(Unit)

        // When
        val result = gestionarStockUseCase(params)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoRepository.obtenerProductoPorId(producto.id) }
        coVerify { mockProductoRepository.actualizarStock(producto.id, cantidadEsperada) }
    }

    @Test
    fun `reducir stock debe restar cantidad correctamente`() = runTest {
        // Given
        val producto = TestData.productoTest1.copy(cantidadActual = 50)
        val params = MovimientoStockParams(
            productoId = producto.id,
            cantidad = 15,
            tipoMovimiento = TipoMovimiento.SALIDA,
            motivo = "Venta"
        )
        val cantidadEsperada = 35

        coEvery { mockProductoRepository.obtenerProductoPorId(producto.id) } returns producto
        coEvery { mockProductoRepository.actualizarStock(producto.id, cantidadEsperada) } returns PADomainState.Success(Unit)

        // When
        val result = gestionarStockUseCase(params)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoRepository.actualizarStock(producto.id, cantidadEsperada) }
    }

    @Test
    fun `reducir stock con cantidad insuficiente debe retornar error`() = runTest {
        // Given
        val producto = TestData.productoTest1.copy(cantidadActual = 10)
        val params = MovimientoStockParams(
            productoId = producto.id,
            cantidad = 15, // Más de lo disponible
            tipoMovimiento = TipoMovimiento.SALIDA,
            motivo = "Venta"
        )

        coEvery { mockProductoRepository.obtenerProductoPorId(producto.id) } returns producto

        // When
        val result = gestionarStockUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.StockInsuficiente::class.java)

        coVerify(exactly = 0) { mockProductoRepository.actualizarStock(any(), any()) }
    }

    @Test
    fun `ajustar stock debe establecer cantidad exacta`() = runTest {
        // Given
        val producto = TestData.productoTest1
        val params = MovimientoStockParams(
            productoId = producto.id,
            cantidad = 100,
            tipoMovimiento = TipoMovimiento.AJUSTE,
            motivo = "Inventario físico"
        )

        coEvery { mockProductoRepository.obtenerProductoPorId(producto.id) } returns producto
        coEvery { mockProductoRepository.actualizarStock(producto.id, 100) } returns PADomainState.Success(Unit)

        // When
        val result = gestionarStockUseCase(params)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoRepository.actualizarStock(producto.id, 100) }
    }

    @Test
    fun `ajustar stock con cantidad negativa debe retornar error`() = runTest {
        // Given
        val producto = TestData.productoTest1
        val params = MovimientoStockParams(
            productoId = producto.id,
            cantidad = -10,
            tipoMovimiento = TipoMovimiento.AJUSTE,
            motivo = "Ajuste"
        )

        coEvery { mockProductoRepository.obtenerProductoPorId(producto.id) } returns producto

        // When
        val result = gestionarStockUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.CantidadInvalida::class.java)
    }

    @Test
    fun `gestionar stock con producto inexistente debe retornar error`() = runTest {
        // Given
        val productoId = 999L
        val params = MovimientoStockParams(
            productoId = productoId,
            cantidad = 10,
            tipoMovimiento = TipoMovimiento.ENTRADA,
            motivo = "Test"
        )

        coEvery { mockProductoRepository.obtenerProductoPorId(productoId) } returns null

        // When
        val result = gestionarStockUseCase(params)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.ProductoNoEncontrado::class.java)
        Truth.assertThat(error.message).contains(productoId.toString())
    }
}