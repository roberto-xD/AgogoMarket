package com.passioagogo.market.useCase

import com.google.common.truth.Truth
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.usecase.BuscarProductosParams
import com.passioagogo.market.domain.usecase.BuscarProductosUseCase
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
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
class BuscarProductosUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockProductoRepository: IProductoRepository = mockk()
    private val buscarProductosUseCase = BuscarProductosUseCase(mockProductoRepository)

    @Test
    fun `buscar productos debe retornar resultados coincidentes`() = runTest {
        // Given
        val query = "Test"
        val params = BuscarProductosParams(query = query)
        val productosEsperados = listOf(TestData.productoTest1, TestData.productoTest2)
        val flowResultados = flowOf(productosEsperados)

        coEvery { mockProductoRepository.buscarProductos(query) } returns flowResultados

        // When
        val result = buscarProductosUseCase(params)

        // Then
        val flow = result.shouldBeSuccess()

        flow.test {
            val productos = awaitItem()
            Truth.assertThat(productos).hasSize(2)
            Truth.assertThat(productos).containsExactly(TestData.productoTest1, TestData.productoTest2)
            awaitComplete()
        }

        coVerify { mockProductoRepository.buscarProductos(query) }
    }

    @Test
    fun `buscar productos con query vacio debe retornar flow vacio`() = runTest {
        // Given
        val params = BuscarProductosParams(query = "")
        val flowVacio = flowOf(emptyList<Producto>())

        coEvery { mockProductoRepository.buscarProductos("") } returns flowVacio

        // When
        val result = buscarProductosUseCase(params)

        // Then
        val flow = result.shouldBeSuccess()

        flow.test {
            val productos = awaitItem()
            Truth.assertThat(productos).isEmpty()
            awaitComplete()
        }
    }
}