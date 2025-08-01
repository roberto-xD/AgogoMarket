package com.passioagogo.market.implementation

import com.google.common.truth.Truth
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.domain.implementation.CategoriaRepositoryDomainImpl
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.shouldBeSuccess
import com.passioagogo.market.testing.test
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
class CategoriaRepositoryDomainImplTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockCategoriaDao: CategoriaDao = mockk()
    private val repository = CategoriaRepositoryDomainImpl(mockCategoriaDao)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `obtenerTodasLasCategorias debe retornar flow de categorias mapeadas`() = runTest {
        // Given
        val categoriasEntity = listOf(
            TestData.categoriaTest1.toEntity(),
            TestData.categoriaTest2.toEntity()
        )
        val flowEntity = flowOf(categoriasEntity)

        every { mockCategoriaDao.obtenerCategoriasActivas() } returns flowEntity

        // When
        val result = repository.obtenerTodasLasCategorias()

        // Then
        result.test {
            val categorias = awaitItem()
            Truth.assertThat(categorias).hasSize(2)
            Truth.assertThat(categorias[0].nombre).isEqualTo(TestData.categoriaTest1.nombre)
            awaitComplete()
        }

        verify { mockCategoriaDao.obtenerCategoriasActivas() }
    }

    @Test
    fun `obtenerCategoriasPorFamilia debe filtrar por familia`() = runTest {
        // Given
        val familiaId = 1L
        val categoriasEntity = listOf(TestData.categoriaTest1.toEntity())
        val flowEntity = flowOf(categoriasEntity)

        every { mockCategoriaDao.obtenerCategoriasPorFamilia(familiaId) } returns flowEntity

        // When
        val result = repository.obtenerCategoriasPorFamilia(familiaId)

        // Then
        result.test {
            val categorias = awaitItem()
            Truth.assertThat(categorias).hasSize(1)
            Truth.assertThat(categorias[0].familiaId).isEqualTo(familiaId)
            awaitComplete()
        }

        verify { mockCategoriaDao.obtenerCategoriasPorFamilia(familiaId) }
    }

    @Test
    fun `guardarCategoria debe retornar Success con id`() = runTest {
        // Given
        val categoria = TestData.categoriaTest1
        val expectedId = 1L

        coEvery { mockCategoriaDao.insertarCategoria(any()) } returns expectedId

        // When
        val result = repository.guardarCategoria(categoria)

        // Then
        val id = result.shouldBeSuccess()
        Truth.assertThat(id).isEqualTo(expectedId)

        coVerify { mockCategoriaDao.insertarCategoria(any()) }
    }

    @Test
    fun `actualizarCategoria debe retornar Success`() = runTest {
        // Given
        val categoria = TestData.categoriaTest1

        coEvery { mockCategoriaDao.actualizarCategoria(any()) } just Runs

        // When
        val result = repository.actualizarCategoria(categoria)

        // Then
        result.shouldBeSuccess()

        coVerify { mockCategoriaDao.actualizarCategoria(any()) }
    }

    @Test
    fun `eliminarCategoria debe hacer soft delete`() = runTest {
        // Given
        val categoriaId = 1L

        coEvery { mockCategoriaDao.eliminarCategoria(categoriaId) } just Runs

        // When
        val result = repository.eliminarCategoria(categoriaId)

        // Then
        result.shouldBeSuccess()

        coVerify { mockCategoriaDao.eliminarCategoria(categoriaId) }
    }
}