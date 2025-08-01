package com.passioagogo.market.testing

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.Proveedor

class ProductoTestBuilder {
    private var id: Long = 1L
    private var nombre: String = "Producto Test"
    private var descripcion: String? = "Descripción test"
    private var skuInterno: String = "TEST001"
    private var codigoBarras: String? = "1234567890123"
    private var precioCompra: Double = 10.0
    private var precioVenta: Double = 15.0
    private var cantidadActual: Int = 50
    private var cantidadMinima: Int = 10
    private var cantidadMaximaComprada: Int = 100
    private var proveedorPrincipalId: Long? = 1L
    private var color: String? = null
    private var fechaUltimaVenta: Long? = null
    private var fechaUltimaCompra: Long? = null
    private var activo: Boolean = true

    fun withId(id: Long) = apply { this.id = id }
    fun withNombre(nombre: String) = apply { this.nombre = nombre }
    fun withSku(sku: String) = apply { this.skuInterno = sku }
    fun withCodigoBarras(codigo: String?) = apply { this.codigoBarras = codigo }
    fun withPrecioCompra(precio: Double) = apply { this.precioCompra = precio }
    fun withPrecioVenta(precio: Double) = apply { this.precioVenta = precio }
    fun withCantidadActual(cantidad: Int) = apply { this.cantidadActual = cantidad }
    fun withCantidadMinima(cantidad: Int) = apply { this.cantidadMinima = cantidad }
    fun withStockBajo() = apply { this.cantidadActual = this.cantidadMinima - 1 }
    fun withFechaUltimaVenta(fecha: Long?) = apply { this.fechaUltimaVenta = fecha }
    fun withActivo(activo: Boolean) = apply { this.activo = activo }

    fun build() = Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        skuInterno = skuInterno,
        codigoBarras = codigoBarras,
        precioCompra = precioCompra,
        precioVenta = precioVenta,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        cantidadMaximaComprada = cantidadMaximaComprada,
        proveedorPrincipalId = proveedorPrincipalId,
        color = color,
        fechaUltimaVenta = fechaUltimaVenta,
        fechaUltimaCompra = fechaUltimaCompra,
        activo = activo
    )
}

fun productoTest() = ProductoTestBuilder()

class CategoriaTestBuilder {
    private var id: Long = 1L
    private var nombre: String = "Categoría Test"
    private var descripcion: String? = "Descripción test"
    private var familiaId: Long = 1L
    private var activo: Boolean = true

    fun withId(id: Long) = apply { this.id = id }
    fun withNombre(nombre: String) = apply { this.nombre = nombre }
    fun withFamiliaId(familiaId: Long) = apply { this.familiaId = familiaId }
    fun withActivo(activo: Boolean) = apply { this.activo = activo }

    fun build() = Categoria(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        familiaId = familiaId,
        activo = activo
    )
}

fun categoriaTest() = CategoriaTestBuilder()

class ProveedorTestBuilder {
    private var id: Long = 1L
    private var nombre: String = "Proveedor Test"
    private var contacto: String? = "Contacto Test"
    private var telefono: String? = "123456789"
    private var email: String? = "test@proveedor.com"
    private var direccion: String? = "Dirección Test"
    private var activo: Boolean = true

    fun withId(id: Long) = apply { this.id = id }
    fun withNombre(nombre: String) = apply { this.nombre = nombre }
    fun withEmail(email: String?) = apply { this.email = email }
    fun withActivo(activo: Boolean) = apply { this.activo = activo }

    fun build() = Proveedor(
        id = id,
        nombre = nombre,
        contacto = contacto,
        telefono = telefono,
        email = email,
        direccion = direccion,
        activo = activo
    )
}

fun proveedorTest() = ProveedorTestBuilder()