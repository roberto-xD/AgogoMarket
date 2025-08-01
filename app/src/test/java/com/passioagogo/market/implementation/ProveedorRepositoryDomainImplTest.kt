package com.passioagogo.market.implementation

import com.google.common.truth.Truth
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.entity.utils.ProductoProveedorConNombre
import com.passioagogo.market.domain.implementation.ProveedorRepositoryDomainImpl
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
class ProveedorRepositoryDomainImplTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockProveedorDao: ProveedorDao = mockk()
    private val mockProductoProveedorDao: ProductoProveedorDao = mockk()
    private val repository = ProveedorRepositoryDomainImpl(
        mockProveedorDao,
        mockProductoProveedorDao
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `obtenerTodosLosProveedores debe retornar flow de proveedores mapeados`() = runTest {
        // Given
        val proveedoresEntity = listOf(
            TestData.proveedorTest1.toEntity(),
            TestData.proveedorTest2.toEntity()
        )
        val flowEntity = flowOf(proveedoresEntity)

        every { mockProveedorDao.obtenerProveedoresActivos() } returns flowEntity

        // When
        val result = repository.obtenerTodosLosProveedores()

        // Then
        result.test {
            val proveedores = awaitItem()
            Truth.assertThat(proveedores).hasSize(2)
            Truth.assertThat(proveedores[0].nombre).isEqualTo(TestData.proveedorTest1.nombre)
            awaitComplete()
        }

        verify { mockProveedorDao.obtenerProveedoresActivos() }
    }

    @Test
    fun `obtenerProveedoresPorProducto debe retornar proveedores con precios`() = runTest {
        // Given
        val productoId = 1L
        val proveedorConNombre = ProductoProveedorConNombre(
            productoId = productoId,
            proveedorId = 1L,
            precioCompra = 15.0,
            fechaUltimaCompra = System.currentTimeMillis(),
            activo = true,
            nombreProveedor = "Proveedor Test"
        )
        val proveedorEntity = TestData.proveedorTest1.toEntity()

        coEvery { mockProductoProveedorDao.obtenerProveedoresPorProducto(productoId) } returns listOf(proveedorConNombre)
        coEvery { mockProveedorDao.obtenerProveedorPorId(1L) } returns proveedorEntity

        // When
        val result = repository.obtenerProveedoresPorProducto(productoId)

        // Then
        Truth.assertThat(result).hasSize(1)
        val proveedorConPrecio = result[0]
        Truth.assertThat(proveedorConPrecio.proveedor.id).isEqualTo(1L)
        Truth.assertThat(proveedorConPrecio.precioCompra).isEqualTo(15.0)

        coVerify { mockProductoProveedorDao.obtenerProveedoresPorProducto(productoId) }
        coVerify { mockProveedorDao.obtenerProveedorPorId(1L) }
    }

    @Test
    fun `asociarProveedorConProducto debe crear relacion correctamente`() = runTest {
        // Given
        val productoId = 1L
        val proveedorId = 1L
        val precioCompra = 20.0

        coEvery { mockProductoProveedorDao.insertarProductoProveedor(any()) } just Runs

        // When
        val result = repository.asociarProveedorConProducto(productoId, proveedorId, precioCompra)

        // Then
        result.shouldBeSuccess()

        coVerify {
            mockProductoProveedorDao.insertarProductoProveedor(
                match { productoProveedor ->
                    productoProveedor.productoId == productoId &&
                            productoProveedor.proveedorId == proveedorId &&
                            productoProveedor.precioCompra == precioCompra
                }
            )
        }
    }
}