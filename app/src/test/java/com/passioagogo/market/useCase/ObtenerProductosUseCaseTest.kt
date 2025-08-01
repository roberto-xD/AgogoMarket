package com.passioagogo.market.useCase

import com.google.common.truth.Truth
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.usecase.ObtenerProductosUseCase
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.shouldBeError
import com.passioagogo.market.testing.shouldBeSuccess
import com.passioagogo.market.testing.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ObtenerProductosUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockProductoRepository: IProductoRepository = mockk()
    private val obtenerProductosUseCase = ObtenerProductosUseCase(mockProductoRepository)

    @Test
    fun `obtener productos debe retornar flow de productos`() = runTest {
        // Given
        val productosEsperados = listOf(TestData.productoTest1, TestData.productoTest2)
        val flowProductos = flowOf(productosEsperados)

        coEvery { mockProductoRepository.obtenerTodosLosProductos() } returns flowProductos

        // When
        val result = obtenerProductosUseCase()

        // Then
        val flow = result.shouldBeSuccess()

        flow.test {
            val productos = awaitItem()
            Truth.assertThat(productos).hasSize(2)
            Truth.assertThat(productos).containsExactly(TestData.productoTest1, TestData.productoTest2)
            awaitComplete()
        }

        coVerify { mockProductoRepository.obtenerTodosLosProductos() }
    }

    @Test
    fun `obtener productos cuando repository falla debe retornar error`() = runTest {
        // Given
        val expectedException = RuntimeException("Database error")
        coEvery { mockProductoRepository.obtenerTodosLosProductos() } throws expectedException

        // When
        val result = obtenerProductosUseCase()

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isEqualTo(expectedException)
    }
}