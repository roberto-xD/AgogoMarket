package com.passioagogo.market.models

import com.google.common.truth.Truth
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.testing.TestData
import com.passioagogo.market.testing.productoTest
import org.junit.Test

class ProductoTest {

    @Test
    fun `margenGanancia debe calcular porcentaje correcto`() {
        // Given
        val producto = productoTest()
            .withPrecioCompra(10.0)
            .withPrecioVenta(15.0)
            .build()

        // When
        val margen = producto.margenGanancia

        // Then
        Truth.assertThat(margen).isEqualTo(50.0) // (15-10)/10 * 100 = 50%
    }

    @Test
    fun `margenGanancia debe retornar cero cuando precio compra es cero`() {
        // Given
        val producto = productoTest()
            .withPrecioCompra(0.0)
            .withPrecioVenta(15.0)
            .build()

        // When
        val margen = producto.margenGanancia

        // Then
        Truth.assertThat(margen).isEqualTo(0.0)
    }

    @Test
    fun `valorInventario debe calcular correctamente`() {
        // Given
        val producto = productoTest()
            .withPrecioCompra(25.0)
            .withCantidadActual(10)
            .build()

        // When
        val valor = producto.valorInventario

        // Then
        Truth.assertThat(valor).isEqualTo(250.0) // 25 * 10 = 250
    }

    @Test
    fun `necesitaRestock debe retornar true cuando cantidad actual menor o igual a minima`() {
        // Given
        val producto = productoTest()
            .withCantidadActual(5)
            .withCantidadMinima(10)
            .build()

        // When & Then
        Truth.assertThat(producto.necesitaRestock).isTrue()
    }

    @Test
    fun `necesitaRestock debe retornar false cuando cantidad actual mayor a minima`() {
        // Given
        val producto = productoTest()
            .withCantidadActual(15)
            .withCantidadMinima(10)
            .build()

        // When & Then
        Truth.assertThat(producto.necesitaRestock).isFalse()
    }

    @Test
    fun `tiempoSinVender debe calcular diferencia correctamente`() {
        // Given
        val hace3Dias = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
        val producto = productoTest()
            .withFechaUltimaVenta(hace3Dias)
            .build()

        // When
        val tiempo = producto.tiempoSinVender

        // Then
        Truth.assertThat(tiempo).isNotNull()
        Truth.assertThat(tiempo!!).isGreaterThan(2 * 24 * 60 * 60 * 1000L) // Más de 2 días
        Truth.assertThat(tiempo).isLessThan(4 * 24 * 60 * 60 * 1000L) // Menos de 4 días
    }

    @Test
    fun `tiempoSinVender debe retornar null cuando no hay fecha de venta`() {
        // Given
        val producto = productoTest()
            .withFechaUltimaVenta(null)
            .build()

        // When
        val tiempo = producto.tiempoSinVender

        // Then
        Truth.assertThat(tiempo).isNull()
    }

    @Test
    fun `fromEntity debe mapear correctamente desde entity`() {
        // Given
        val entity = ProductoEntity(
            id = 1L,
            nombre = "Test Producto",
            descripcion = "Test descripción",
            skuInterno = "TEST001",
            codigoBarras = "123456",
            precioCompra = 10.0,
            precioVenta = 15.0,
            cantidadActual = 50,
            cantidadMinima = 10,
            cantidadMaximaComprada = 100,
            proveedorPrincipalId = 1L,
            color = "Rojo",
            fechaUltimaVenta = 123456789L,
            fechaUltimaCompra = 123456788L,
            activo = true,
            fechaCreacion = 123456787L,
            fechaActualizacion = 123456790L
        )

        // When
        val producto = Producto.fromEntity(entity)

        // Then
        Truth.assertThat(producto.id).isEqualTo(entity.id)
        Truth.assertThat(producto.nombre).isEqualTo(entity.nombre)
        Truth.assertThat(producto.descripcion).isEqualTo(entity.descripcion)
        Truth.assertThat(producto.skuInterno).isEqualTo(entity.skuInterno)
        Truth.assertThat(producto.codigoBarras).isEqualTo(entity.codigoBarras)
        Truth.assertThat(producto.precioCompra).isEqualTo(entity.precioCompra)
        Truth.assertThat(producto.precioVenta).isEqualTo(entity.precioVenta)
    }

    @Test
    fun `toEntity debe mapear correctamente a entity`() {
        // Given
        val producto = TestData.productoTest1

        // When
        val entity = producto.toEntity()

        // Then
        Truth.assertThat(entity.id).isEqualTo(producto.id)
        Truth.assertThat(entity.nombre).isEqualTo(producto.nombre)
        Truth.assertThat(entity.descripcion).isEqualTo(producto.descripcion)
        Truth.assertThat(entity.skuInterno).isEqualTo(producto.skuInterno)
        Truth.assertThat(entity.codigoBarras).isEqualTo(producto.codigoBarras)
        Truth.assertThat(entity.precioCompra).isEqualTo(producto.precioCompra)
        Truth.assertThat(entity.precioVenta).isEqualTo(producto.precioVenta)
    }
}