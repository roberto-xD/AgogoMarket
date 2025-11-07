package com.passioagogo.market.data.repository

import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoFamiliaDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.data.local.entity.relation.ProductoCategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoFamiliaEntity
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import com.passioagogo.market.domain.bean.AtributoProducto
import com.passioagogo.market.domain.bean.Categoria
import com.passioagogo.market.domain.bean.ImagenProducto
import com.passioagogo.market.domain.bean.Producto
import com.passioagogo.market.domain.bean.ProductoDetallado
import com.passioagogo.market.domain.bean.Proveedor
import com.passioagogo.market.domain.bean.ProveedorConPrecio
import com.passioagogo.market.domain.bean.Subcategoria
import com.passioagogo.market.domain.bean.TipoDatoAtributo
import com.passioagogo.market.domain.repository.IProductoRepository
import com.passioagogo.market.domain.state.DomainException
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.ui.decorators.orZero
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoRepositoryDomainImpl @Inject constructor(
    private val productoDao: ProductoDao,
    private val productoFamiliaDao: ProductoFamiliaDao,
    private val productoCategoriaDao: ProductoCategoriaDao,
    private val productoSubcategoriaDao: ProductoSubcategoriaDao,
    private val productoProveedorDao: ProductoProveedorDao,
    private val productoAtributoDao: ProductoAtributoDao,
    private val productoImagenDao: ProductoImagenDao,
    private val categoriaDao: CategoriaDao,
    private val subcategoriaDao: SubcategoriaDao,
    private val proveedorDao: ProveedorDao
) : IProductoRepository {

    override suspend fun obtenerTodosLosProductos(): Flow<List<Producto>> {
        return productoDao.obtenerProductosActivos().map { entities ->
            entities.map { Producto.fromEntity(it.producto).copy(imagenPrincipal = it.imagenes.firstOrNull()?.rutaImagen.orEmpty()) }
        }
    }

    override suspend fun obtenerProductoPorId(id: Long): Producto? {
        return productoDao.obtenerProductoPorId(id)?.let { Producto.fromEntity(it) }
    }

    override suspend fun obtenerProductoPorSku(sku: String): Producto? {
        return productoDao.obtenerProductoPorSku(sku)?.let { Producto.fromEntity(it) }
    }

    override suspend fun obtenerProductoPorCodigoBarras(codigo: String): Producto? {
        return productoDao.obtenerProductoPorCodigoBarras(codigo)?.let { Producto.fromEntity(it) }
    }

    override suspend fun obtenerProductosConStockBajo(): Flow<List<Producto>> {
        return productoDao.obtenerProductosConStockBajo().map { entities ->
            entities.map { Producto.fromEntity(it) }
        }
    }

    override suspend fun obtenerProductosPorCategoria(categoriaId: Long): Flow<List<Producto>> {
        return productoDao.obtenerProductosPorCategoria(categoriaId).map { entities ->
            entities.map { Producto.fromEntity(it) }
        }
    }

    override suspend fun obtenerProductoDetallado(id: Long): ProductoDetallado? {
        val productoEntity = productoDao.obtenerProductoPorId(id) ?: return null
        val producto = Producto.fromEntity(productoEntity)

        val familia = productoFamiliaDao.obtenerFamiliaPorProducto(id)?.familiaId.orZero()

        val categorias = productoCategoriaDao.obtenerCategoriasPorProducto(id)
            .map { Categoria.fromEntity(it) }

        val subcategorias = productoSubcategoriaDao.obtenerSubcategoriasPorProducto(id)
            .map { Subcategoria.fromEntity(it) }

        val proveedoresConPrecio = productoProveedorDao.obtenerProveedoresPorProducto(id)
            .mapNotNull { proveedorConNombre ->
                proveedorDao.obtenerProveedorPorId(proveedorConNombre.proveedorId)?.let { proveedorEntity ->
                    ProveedorConPrecio(
                        proveedor = Proveedor.fromEntity(proveedorEntity),
                        precioCompra = proveedorConNombre.precioCompra,
                        fechaUltimaCompra = proveedorConNombre.fechaUltimaCompra,
                        activo = proveedorConNombre.activo
                    )
                }
            }

        val atributos = productoAtributoDao.obtenerAtributosPorProducto(id)
            .map { atributoConTipo ->
                AtributoProducto(
                    id = atributoConTipo.id,
                    nombre = atributoConTipo.nombreAtributo,
                    valor = atributoConTipo.valor,
                    tipoDato = when (atributoConTipo.tipoDato) {
                        "TEXT" -> TipoDatoAtributo.TEXT
                        "NUMBER" -> TipoDatoAtributo.NUMBER
                        "BOOLEAN" -> TipoDatoAtributo.BOOLEAN
                        "DATE" -> TipoDatoAtributo.DATE
                        else -> TipoDatoAtributo.TEXT
                    }
                )
            }

        val imagenes = productoImagenDao.obtenerImagenesPorProducto(id)
            .map { imagenEntity ->
                ImagenProducto(
                    id = imagenEntity.id,
                    rutaImagen = imagenEntity.rutaImagen,
                    orden = imagenEntity.orden,
                    esPrincipal = imagenEntity.esPrincipal,
                    fechaCreacion = imagenEntity.fechaCreacion
                )
            }

        return ProductoDetallado(
            producto = producto,
            familia = familia,
            categorias = categorias.map { it.id },
            subcategorias = subcategorias.map { it.id },
            proveedores = proveedoresConPrecio,
            atributos = atributos,
            imagenes = imagenes
        )
    }

    override suspend fun guardarProducto(producto: Producto): PADomainState<Long> {
        return try {
            val id = productoDao.insertarProducto(producto.toEntity())
            PADomainState.Success(id)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun guardarProductoDetallado(productoDetallado: ProductoDetallado): PADomainState<Long> {
        return try {
            val entity = productoDetallado.producto.toEntity().copy(
                fechaActualizacion = System.currentTimeMillis()
            )
            val productoId = productoDao.insertarProducto(entity)

            // Guardar relaciones
            val productoFamilia = ProductoFamiliaEntity(
                familiaId = productoDetallado.familia,
                productoId = productoId
            )
            productoFamiliaDao.insertarProductoFamilia(productoFamilia)

            val productosCategorias = productoDetallado.categorias.map { categoria ->
                ProductoCategoriaEntity(
                    productoId = productoId,
                    categoriaId = categoria,
                    esPrincipal = false
                )
            }
            productoCategoriaDao.insertarProductoCategorias(productosCategorias)

//            val productosSubcategorias = productoDetallado.subcategorias.map { subcategoria ->
//                ProductoSubcategoriaEntity(
//                    productoId = productoId,
//                    subcategoriaId = subcategoria
//                )
//            }
//            productoSubcategoriaDao.insertarProductoSubcategorias(productosSubcategorias)

            // Guardar imágenes
            productoDetallado.imagenes.forEach { imagen ->
                productoImagenDao.insertarImagen(
                    ProductoImagenEntity(
                        productoId = productoId,
                        rutaImagen = imagen.rutaImagen,
                        orden = imagen.orden,
                        esPrincipal = imagen.orden == 0,
                        fechaCreacion = imagen.fechaCreacion
                    )
                )
            }

            PADomainState.Success(productoId)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarProducto(producto: Producto): PADomainState<Unit> {
        return try {
            val entity = producto.toEntity().copy(fechaActualizacion = System.currentTimeMillis())
            productoDao.actualizarProducto(entity)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarProductoDetallado(productoDetallado: ProductoDetallado): PADomainState<Unit> {
        return try {
            val productoId = productoDetallado.producto.id

            // Actualizar producto base
            val entity = productoDetallado.producto.toEntity().copy(
                fechaActualizacion = System.currentTimeMillis()
            )
            productoDao.actualizarProducto(entity)

            // Actualizar relaciones (eliminar y recrear)
            productoFamiliaDao.eliminarFamiliaDeProducto(productoId)
            productoCategoriaDao.eliminarCategoriasDeProducto(productoId)
            productoSubcategoriaDao.eliminarSubcategoriasDeProducto(productoId)

            val productoFamilia = ProductoFamiliaEntity(
                familiaId = productoDetallado.familia,
                productoId = productoId
            )
            productoFamiliaDao.insertarProductoFamilia(productoFamilia)

            val productosCategorias = productoDetallado.categorias.map { categoria ->
                ProductoCategoriaEntity(
                    productoId = productoId,
                    categoriaId = categoria,
                    esPrincipal = false
                )
            }
            productoCategoriaDao.insertarProductoCategorias(productosCategorias)

//            val productosSubcategorias = productoDetallado.subcategorias.map { subcategoria ->
//                ProductoSubcategoriaEntity(
//                    productoId = productoId,
//                    subcategoriaId = subcategoria
//                )
//            }
//            productoSubcategoriaDao.insertarProductoSubcategorias(productosSubcategorias)

            productoDetallado.imagenes.forEach { imagen ->
                productoImagenDao.insertarImagen(
                    ProductoImagenEntity(
                        productoId = productoId,
                        rutaImagen = imagen.rutaImagen,
                        orden = imagen.orden,
                        esPrincipal = imagen.esPrincipal,
                        fechaCreacion = imagen.fechaCreacion
                    )
                )
            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun eliminarProducto(id: Long): PADomainState<Unit> {
        return try {
            productoDao.eliminarProducto(id)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun actualizarStock(id: Long, nuevaCantidad: Int): PADomainState<Unit> {
        return try {
            productoDao.actualizarStock(id, nuevaCantidad)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun reducirStock(id: Long, cantidad: Int): PADomainState<Unit> {
        return try {
            val producto = productoDao.obtenerProductoPorId(id)
                ?: throw DomainException.ProductoNoEncontrado("ID: $id")

            val nuevaCantidad = producto.cantidadActual.orZero() - cantidad
            if (nuevaCantidad < 0) {
                throw DomainException.StockInsuficiente(
                    producto.nombre,
                    producto.cantidadActual.orZero(),
                    cantidad
                )
            }

            productoDao.actualizarStock(id, nuevaCantidad)
            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun aumentarStock(id: Long, cantidad: Int): PADomainState<Unit> {
        return try {
            val producto = productoDao.obtenerProductoPorId(id)
                ?: throw DomainException.ProductoNoEncontrado("ID: $id")

            val nuevaCantidad = producto.cantidadActual.orZero() + cantidad
            productoDao.actualizarStock(id, nuevaCantidad)

            // Actualizar cantidad máxima comprada si es necesario
            if (nuevaCantidad > producto.cantidadMaximaComprada.orZero()) {
                val productoActualizado = producto.copy(cantidadMaximaComprada = nuevaCantidad)
                productoDao.actualizarProducto(productoActualizado)
            }

            PADomainState.Success(Unit)
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun registrarVenta(id: Long, cantidad: Int): PADomainState<Unit> {
        return try {
            val resultado = reducirStock(id, cantidad)
            if (resultado is PADomainState.Success) {
                productoDao.actualizarFechaUltimaVenta(id, System.currentTimeMillis())
            }
            resultado
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun registrarCompra(id: Long, cantidad: Int): PADomainState<Unit> {
        return try {
            val resultado = aumentarStock(id, cantidad)
            if (resultado is PADomainState.Success) {
                productoDao.actualizarFechaUltimaCompra(id, System.currentTimeMillis())
            }
            resultado
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    override suspend fun buscarProductos(query: String): Flow<List<Producto>> {
        return productoDao.buscarProductos(query).map { productos ->
            productos.map { Producto.fromEntity(it.producto).copy(imagenPrincipal = it.imagenes.firstOrNull()?.rutaImagen.orEmpty()) }
        }
    }
}