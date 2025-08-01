package com.passioagogo.market.models

import com.google.common.truth.Truth
import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.testing.TestData
import org.junit.Test

class ProductoDetalladoTest {

    @Test
    fun `categoriaPrincipal debe retornar primera categoria cuando existe`() {
        // Given
        val categoria1 = TestData.categoriaTest1
        val categoria2 = TestData.categoriaTest2
        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            categorias = listOf(categoria1, categoria2)
        )

        // When
        val categoriaPrincipal = productoDetallado.categoriaPrincipal

        // Then
        Truth.assertThat(categoriaPrincipal).isEqualTo(categoria1)
    }

    @Test
    fun `categoriaPrincipal debe retornar null cuando no hay categorias`() {
        // Given
        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            categorias = emptyList()
        )

        // When
        val categoriaPrincipal = productoDetallado.categoriaPrincipal

        // Then
        Truth.assertThat(categoriaPrincipal).isNull()
    }

    @Test
    fun `imagenPrincipal debe retornar imagen marcada como principal`() {
        // Given
        val imagen1 = ImagenProducto(id = 1L, rutaImagen = "/path1", esPrincipal = false)
        val imagen2 = ImagenProducto(id = 2L, rutaImagen = "/path2", esPrincipal = true)
        val imagen3 = ImagenProducto(id = 3L, rutaImagen = "/path3", esPrincipal = false)

        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            imagenes = listOf(imagen1, imagen2, imagen3)
        )

        // When
        val imagenPrincipal = productoDetallado.imagenPrincipal

        // Then
        Truth.assertThat(imagenPrincipal).isEqualTo(imagen2)
    }

    @Test
    fun `imagenPrincipal debe retornar primera imagen cuando ninguna es principal`() {
        // Given
        val imagen1 = ImagenProducto(id = 1L, rutaImagen = "/path1", esPrincipal = false)
        val imagen2 = ImagenProducto(id = 2L, rutaImagen = "/path2", esPrincipal = false)

        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            imagenes = listOf(imagen1, imagen2)
        )

        // When
        val imagenPrincipal = productoDetallado.imagenPrincipal

        // Then
        Truth.assertThat(imagenPrincipal).isEqualTo(imagen1)
    }

    @Test
    fun `proveedorMasBarato debe retornar proveedor con menor precio`() {
        // Given
        val proveedor1 = ProveedorConPrecio(TestData.proveedorTest1, 15.0, null)
        val proveedor2 = ProveedorConPrecio(TestData.proveedorTest2, 12.0, null)
        val proveedor3 = ProveedorConPrecio(TestData.proveedorTest1.copy(id = 3L), 18.0, null)

        val productoDetallado = ProductoDetallado(
            producto = TestData.productoTest1,
            proveedores = listOf(proveedor1, proveedor2, proveedor3)
        )

        // When
        val proveedorMasBarato = productoDetallado.proveedorMasBarato

        // Then
        Truth.assertThat(proveedorMasBarato).isEqualTo(proveedor2)
        Truth.assertThat(proveedorMasBarato?.precioCompra).isEqualTo(12.0)
    }
}