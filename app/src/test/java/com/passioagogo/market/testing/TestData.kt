package com.passioagogo.market.testing

import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.Familia
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.usecase.producto.GuardarProductoParams

object TestData {

    // Productos de prueba
    val productoTest1 = Producto(
        id = 1L,
        nombre = "Producto Test 1",
        descripcion = "Descripción test",
        skuInterno = "TEST001",
        codigoBarras = "1234567890123",
        precioCompra = 10.0,
        precioVenta = 15.0,
        cantidadActual = 50,
        cantidadMinima = 10,
        cantidadMaximaComprada = 100,
        proveedorPrincipalId = 1L,
        color = "Rojo",
        fechaUltimaVenta = System.currentTimeMillis() - 86400000L, // Ayer
        fechaUltimaCompra = System.currentTimeMillis() - 172800000L, // Hace 2 días
        activo = true
    )

    val productoTest2 = Producto(
        id = 2L,
        nombre = "Producto Test 2",
        descripcion = "Descripción test 2",
        skuInterno = "TEST002",
        codigoBarras = "1234567890124",
        precioCompra = 20.0,
        precioVenta = 30.0,
        cantidadActual = 5, // Stock bajo
        cantidadMinima = 10,
        cantidadMaximaComprada = 50,
        proveedorPrincipalId = 2L,
        color = "Azul",
        fechaUltimaVenta = null,
        fechaUltimaCompra = System.currentTimeMillis() - 86400000L,
        activo = true
    )

    val productoInactivo = productoTest1.copy(
        id = 3L,
        nombre = "Producto Inactivo",
        skuInterno = "INACTIVE001",
        activo = false
    )

    // Categorías de prueba
    val categoriaTest1 = Categoria(
        id = 1L,
        nombre = "BDSM",
        descripcion = "Categoría BDSM",
        familiaId = 1L,
        activo = true
    )

    val categoriaTest2 = Categoria(
        id = 2L,
        nombre = "Lubricantes",
        descripcion = "Categoría Lubricantes",
        familiaId = 2L,
        activo = true
    )

    // Proveedores de prueba
    val proveedorTest1 = Proveedor(
        id = 1L,
        nombre = "Proveedor Test 1",
        contacto = "Juan Pérez",
        telefono = "123456789",
        email = "proveedor1@test.com",
        direccion = "Calle Test 123",
        activo = true
    )

    val proveedorTest2 = Proveedor(
        id = 2L,
        nombre = "Proveedor Test 2",
        contacto = "María García",
        telefono = "987654321",
        email = "proveedor2@test.com",
        direccion = "Avenida Test 456",
        activo = true
    )

    // Familias de prueba
    val familiaJuguetes = Familia(
        id = 1L,
        nombre = "juguetes_adultos",
        descripcion = "Juguetes para adultos",
        activo = true
    )

    val familiaConsumibles = Familia(
        id = 2L,
        nombre = "consumibles",
        descripcion = "Productos consumibles",
        activo = true
    )

    // Parámetros de casos de uso
    val crearProductoParamsValidos = GuardarProductoParams(
        nombre = "Nuevo Producto",
        descripcion = "Descripción del nuevo producto",
        skuInterno = "NEW001",
        codigoBarras = "9876543210987",
        precioCompra = 25.0,
        precioVenta = 40.0,
        cantidadInicial = 30,
        cantidadMinima = 5,
        proveedorPrincipalId = 1L,
    )
}