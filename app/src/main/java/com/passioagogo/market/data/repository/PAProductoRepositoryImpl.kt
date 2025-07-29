package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.HistorialPrecioDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.data.local.entity.relation.ProductoCategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoSubcategoriaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoRepositoryImpl @Inject constructor(
    private val productoDao: ProductoDao,
    private val productoCategoriaDao: ProductoCategoriaDao,
    private val productoSubcategoriaDao: ProductoSubcategoriaDao,
    private val productoProveedorDao: ProductoProveedorDao,
    private val productoAtributoDao: ProductoAtributoDao,
    private val productoImagenDao: ProductoImagenDao,
    private val historialPrecioDao: HistorialPrecioDao
) : ProductoRepository {

    override fun buscarProductos(searchTerm: String): Flow<List<ProductoEntity>> =
        productoDao.buscarProductos(searchTerm)

    override fun obtenerProductosActivos(): Flow<List<ProductoEntity>> =
        productoDao.obtenerProductosActivos()

    override suspend fun obtenerProductoPorId(id: Long): ProductoEntity? =
        productoDao.obtenerProductoPorId(id)

    override suspend fun obtenerProductoPorCodigoBarras(codigo: String): ProductoEntity? =
        productoDao.obtenerProductoPorCodigoBarras(codigo)

    override suspend fun obtenerProductoPorSku(sku: String): ProductoEntity? =
        productoDao.obtenerProductoPorSku(sku)

    override fun obtenerProductosConStockBajo(): Flow<List<ProductoEntity>> =
        productoDao.obtenerProductosConStockBajo()

    override fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>> =
        productoDao.obtenerProductosPorCategoria(categoriaId)

    override suspend fun insertarProducto(producto: ProductoEntity): Long =
        productoDao.insertarProducto(producto)

    override suspend fun actualizarProducto(producto: ProductoEntity) =
        productoDao.actualizarProducto(producto)

    override suspend fun eliminarProducto(id: Long) =
        productoDao.eliminarProducto(id)

    override suspend fun actualizarStock(id: Long, nuevaCantidad: Int) =
        productoDao.actualizarStock(id, nuevaCantidad)

    override suspend fun actualizarFechaUltimaVenta(id: Long, fecha: Long) =
        productoDao.actualizarFechaUltimaVenta(id, fecha)

    override suspend fun actualizarFechaUltimaCompra(id: Long, fecha: Long) =
        productoDao.actualizarFechaUltimaCompra(id, fecha)

    override suspend fun obtenerProductoCompleto(id: Long): ProductoCompleto? {
        val producto = productoDao.obtenerProductoPorId(id) ?: return null
        val categorias = productoCategoriaDao.obtenerCategoriasPorProducto(id)
        val subcategorias = productoSubcategoriaDao.obtenerSubcategoriasPorProducto(id)
        val proveedores = productoProveedorDao.obtenerProveedoresPorProducto(id)
        val atributos = productoAtributoDao.obtenerAtributosPorProducto(id)
        val imagenes = productoImagenDao.obtenerImagenesPorProducto(id)

        return ProductoCompleto(
            producto = producto,
            categorias = categorias,
            subcategorias = subcategorias,
            proveedores = proveedores,
            atributos = atributos,
            imagenes = imagenes
        )
    }

    override suspend fun guardarProductoCompleto(productoCompleto: ProductoCompleto): Long {
        val productoId = productoDao.insertarProducto(productoCompleto.producto)

        // Guardar categorías
        val productosCategorias = productoCompleto.categorias.map { categoria ->
            ProductoCategoriaEntity(
                productoId = productoId,
                categoriaId = categoria.id,
                esPrincipal = false // Se puede ajustar según lógica de negocio
            )
        }
        productoCategoriaDao.insertarProductoCategorias(productosCategorias)

        // Guardar subcategorías
        val productosSubcategorias = productoCompleto.subcategorias.map { subcategoria ->
            ProductoSubcategoriaEntity(
                productoId = productoId,
                subcategoriaId = subcategoria.id
            )
        }
        productoSubcategoriaDao.insertarProductoSubcategorias(productosSubcategorias)

        // Guardar atributos
        val productosAtributos = productoCompleto.atributos.map { atributo ->
            ProductoAtributoEntity(
                productoId = productoId,
                tipoAtributoId = atributo.tipoAtributoId,
                valor = atributo.valor
            )
        }
        productoAtributoDao.insertarProductoAtributos(productosAtributos)

        // Guardar imágenes
        productoCompleto.imagenes.forEach { imagen ->
            productoImagenDao.insertarImagen(
                imagen.copy(productoId = productoId)
            )
        }

        return productoId
    }

    override suspend fun actualizarProductoCompleto(productoCompleto: ProductoCompleto) {
        val productoId = productoCompleto.producto.id

        // Actualizar producto base
        productoDao.actualizarProducto(productoCompleto.producto)

        // Actualizar relaciones (eliminar existentes y crear nuevas)
        productoCategoriaDao.eliminarCategoriasDeProducto(productoId)
        productoSubcategoriaDao.eliminarSubcategoriasDeProducto(productoId)
        productoAtributoDao.eliminarAtributosDeProducto(productoId)

        // Recrear relaciones
        val productosCategorias = productoCompleto.categorias.map { categoria ->
            ProductoCategoriaEntity(
                productoId = productoId,
                categoriaId = categoria.id,
                esPrincipal = false
            )
        }
        productoCategoriaDao.insertarProductoCategorias(productosCategorias)

        val productosSubcategorias = productoCompleto.subcategorias.map { subcategoria ->
            ProductoSubcategoriaEntity(
                productoId = productoId,
                subcategoriaId = subcategoria.id
            )
        }
        productoSubcategoriaDao.insertarProductoSubcategorias(productosSubcategorias)

        val productosAtributos = productoCompleto.atributos.map { atributo ->
            ProductoAtributoEntity(
                productoId = productoId,
                tipoAtributoId = atributo.tipoAtributoId,
                valor = atributo.valor
            )
        }
        productoAtributoDao.insertarProductoAtributos(productosAtributos)
    }
}