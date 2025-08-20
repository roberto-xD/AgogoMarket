package com.passioagogo.market.implementation

import com.google.common.truth.Truth
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.data.implementation.ProductoRepositoryDomainImpl
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.testing.CoroutineTestRule
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.shouldBeError
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
class ProductoRepositoryDomainImplTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    // Mocks de DAOs
    private val mockProductoDao: ProductoDao = mockk()
    private val mockProductoCategoriaDao: ProductoCategoriaDao = mockk()
    private val mockProductoSubcategoriaDao: ProductoSubcategoriaDao = mockk()
    private val mockProductoProveedorDao: ProductoProveedorDao = mockk()
    private val mockProductoAtributoDao: ProductoAtributoDao = mockk()
    private val mockProductoImagenDao: ProductoImagenDao = mockk()
    private val mockCategoriaDao: CategoriaDao = mockk()
    private val mockSubcategoriaDao: SubcategoriaDao = mockk()
    private val mockProveedorDao: ProveedorDao = mockk()

    private val repository = ProductoRepositoryDomainImpl(
        productoDao = mockProductoDao,
        productoCategoriaDao = mockProductoCategoriaDao,
        productoSubcategoriaDao = mockProductoSubcategoriaDao,
        productoProveedorDao = mockProductoProveedorDao,
        productoAtributoDao = mockProductoAtributoDao,
        productoImagenDao = mockProductoImagenDao,
        categoriaDao = mockCategoriaDao,
        subcategoriaDao = mockSubcategoriaDao,
        proveedorDao = mockProveedorDao
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `obtenerTodosLosProductos debe retornar flow de productos mapeados`() = runTest {
        // Given
        val productosEntity = listOf(
            TestData.productoTest1.toEntity(),
            TestData.productoTest2.toEntity()
        )
        val flowEntity = flowOf(productosEntity)

        every { mockProductoDao.obtenerProductosActivos() } returns flowEntity

        // When
        val result = repository.obtenerTodosLosProductos()

        // Then
        result.test {
            val productos = awaitItem()
            Truth.assertThat(productos).hasSize(2)
            Truth.assertThat(productos[0].id).isEqualTo(TestData.productoTest1.id)
            Truth.assertThat(productos[0].nombre).isEqualTo(TestData.productoTest1.nombre)
            Truth.assertThat(productos[1].id).isEqualTo(TestData.productoTest2.id)
            awaitComplete()
        }

        verify { mockProductoDao.obtenerProductosActivos() }
    }

    @Test
    fun `obtenerProductoPorId debe retornar producto mapeado cuando existe`() = runTest {
        // Given
        val productoId = 1L
        val productoEntity = TestData.productoTest1.toEntity()

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns productoEntity

        // When
        val result = repository.obtenerProductoPorId(productoId)

        // Then
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.id).isEqualTo(TestData.productoTest1.id)
        Truth.assertThat(result?.nombre).isEqualTo(TestData.productoTest1.nombre)

        coVerify { mockProductoDao.obtenerProductoPorId(productoId) }
    }

    @Test
    fun `obtenerProductoPorId debe retornar null cuando no existe`() = runTest {
        // Given
        val productoId = 999L

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns null

        // When
        val result = repository.obtenerProductoPorId(productoId)

        // Then
        Truth.assertThat(result).isNull()

        coVerify { mockProductoDao.obtenerProductoPorId(productoId) }
    }

    @Test
    fun `obtenerProductoPorSku debe retornar producto cuando existe`() = runTest {
        // Given
        val sku = "TEST001"
        val productoEntity = TestData.productoTest1.toEntity()

        coEvery { mockProductoDao.obtenerProductoPorSku(sku) } returns productoEntity

        // When
        val result = repository.obtenerProductoPorSku(sku)

        // Then
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result?.skuInterno).isEqualTo(sku)

        coVerify { mockProductoDao.obtenerProductoPorSku(sku) }
    }

    @Test
    fun `guardarProducto debe retornar Success con id cuando todo va bien`() = runTest {
        // Given
        val producto = TestData.productoTest1
        val expectedId = 1L

        coEvery { mockProductoDao.insertarProducto(any()) } returns expectedId

        // When
        val result = repository.guardarProducto(producto)

        // Then
        val id = result.shouldBeSuccess()
        Truth.assertThat(id).isEqualTo(expectedId)

        coVerify { mockProductoDao.insertarProducto(any()) }
    }

    @Test
    fun `guardarProducto debe retornar Error cuando falla la insercion`() = runTest {
        // Given
        val producto = TestData.productoTest1
        val expectedException = RuntimeException("Database error")

        coEvery { mockProductoDao.insertarProducto(any()) } throws expectedException

        // When
        val result = repository.guardarProducto(producto)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isEqualTo(expectedException)
    }

    @Test
    fun `actualizarStock debe retornar Success cuando actualiza correctamente`() = runTest {
        // Given
        val productoId = 1L
        val nuevaCantidad = 75

        coEvery { mockProductoDao.actualizarStock(productoId, nuevaCantidad) } just Runs

        // When
        val result = repository.actualizarStock(productoId, nuevaCantidad)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoDao.actualizarStock(productoId, nuevaCantidad) }
    }

    @Test
    fun `reducirStock debe verificar stock suficiente antes de reducir`() = runTest {
        // Given
        val productoId = 1L
        val cantidad = 10
        val producto = TestData.productoTest1.copy(cantidadActual = 50).toEntity()
        val nuevaCantidad = 40

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns producto
        coEvery { mockProductoDao.actualizarStock(productoId, nuevaCantidad) } just Runs

        // When
        val result = repository.reducirStock(productoId, cantidad)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoDao.obtenerProductoPorId(productoId) }
        coVerify { mockProductoDao.actualizarStock(productoId, nuevaCantidad) }
    }

    @Test
    fun `reducirStock debe retornar Error cuando stock insuficiente`() = runTest {
        // Given
        val productoId = 1L
        val cantidad = 60 // Más del stock disponible
        val producto = TestData.productoTest1.copy(cantidadActual = 50).toEntity()

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns producto

        // When
        val result = repository.reducirStock(productoId, cantidad)

        // Then
        val error = result.shouldBeError()
        Truth.assertThat(error).isInstanceOf(DomainException.StockInsuficiente::class.java)

        coVerify(exactly = 0) { mockProductoDao.actualizarStock(any(), any()) }
    }

    @Test
    fun `aumentarStock debe actualizar cantidad maxima si es necesario`() = runTest {
        // Given
        val productoId = 1L
        val cantidad = 30
        val producto = TestData.productoTest1.copy(
            cantidadActual = 50,
            cantidadMaximaComprada = 70
        ).toEntity()
        val nuevaCantidad = 80

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns producto
        coEvery { mockProductoDao.actualizarStock(productoId, nuevaCantidad) } just Runs
        coEvery { mockProductoDao.actualizarProducto(any()) } just Runs

        // When
        val result = repository.aumentarStock(productoId, cantidad)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoDao.actualizarStock(productoId, nuevaCantidad) }
        coVerify { mockProductoDao.actualizarProducto(any()) }
    }

    @Test
    fun `registrarVenta debe reducir stock y actualizar fecha`() = runTest {
        // Given
        val productoId = 1L
        val cantidad = 5
        val producto = TestData.productoTest1.copy(cantidadActual = 50).toEntity()

        coEvery { mockProductoDao.obtenerProductoPorId(productoId) } returns producto
        coEvery { mockProductoDao.actualizarStock(productoId, 45) } just Runs
        coEvery { mockProductoDao.actualizarFechaUltimaVenta(productoId, any()) } just Runs

        // When
        val result = repository.registrarVenta(productoId, cantidad)

        // Then
        result.shouldBeSuccess()

        coVerify { mockProductoDao.actualizarStock(productoId, 45) }
        coVerify { mockProductoDao.actualizarFechaUltimaVenta(productoId, any()) }
    }

    @Test
    fun `buscarProductos debe filtrar por nombre, sku y codigo de barras`() = runTest {
        // Given
        val query = "TEST"
        val productosEntity = listOf(
            TestData.productoTest1.toEntity(),
            TestData.productoTest2.toEntity()
        )
        val flowEntity = flowOf(productosEntity)

        every { mockProductoDao.obtenerProductosActivos() } returns flowEntity

        // When
        val result = repository.buscarProductos(query)

        // Then
        result.test {
            val productos = awaitItem()
            // Verificar que se filtran productos que contienen "TEST" en nombre o SKU
            Truth.assertThat(productos).isNotEmpty()
            productos.forEach { producto ->
                val coincide = producto.nombre.contains(query, ignoreCase = true) ||
                        producto.skuInterno.contains(query, ignoreCase = true) ||
                        (producto.descripcion?.contains(query, ignoreCase = true) == true) ||
                        (producto.codigoBarras?.contains(query, ignoreCase = true) == true)
                Truth.assertThat(coincide).isTrue()
            }
            awaitComplete()
        }
    }
}