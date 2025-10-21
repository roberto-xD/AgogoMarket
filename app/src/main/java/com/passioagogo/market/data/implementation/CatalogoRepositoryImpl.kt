package com.passioagogo.market.data.implementation

import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.domain.model.catalogo.CatalogoCompleto
import com.passioagogo.market.domain.model.catalogo.ProductoCatalogo
import com.passioagogo.market.domain.repository.CatalogoRepository
import javax.inject.Inject

class CatalogoRepositoryImpl @Inject constructor(
    private val productoDao: ProductoDao, // Ajusta según tus DAOs
    private val familiaDao: FamiliaDao,
    private val categoriaDao: CategoriaDao,
    private val subcategoriaDao: SubcategoriaDao,
    private val atributoDao: ProductoAtributoDao
) : CatalogoRepository {

    override suspend fun getCatalogoCompleto(): CatalogoCompleto {
//        val familias = familiaDao.obtenerFamiliasActivas()
//
//        val familiasCatalogo = familias.map { familia ->
//            val categorias = categoriaDao.obtenerCategoriasPorFamilia(familia)
//
//            val categoriasCatalogo = categorias.map { categoria ->
//                val subcategorias = subcategoriaDao.getByCategoriaId(categoria.id)
//
//                // Obtener productos de todas las subcategorías de esta categoría
//                val productos = subcategorias.flatMap { subcat ->
//                    val prods = productoDao.getBySubcategoriaId(subcat.id)
//                    prods.map { producto ->
//                        val atributos = atributoDao.getByProductoId(producto.id)
//                        CatalogoMapper.mapProductoToProductoCatalogo(producto, atributos)
//                    }
//                }
//
//                CategoriaCatalogo(
//                    nombre = categoria.nombre,
//                    icono = null, // puedes agregar lógica para íconos
//                    productos = productos
//                )
//            }
//
//            FamiliaCatalogo(
//                nombre = familia.nombre,
//                categorias = categoriasCatalogo
//            )
//        }
//
//        return CatalogoCompleto(
//            familias = familiasCatalogo,
//            logo = "logo_passion_agogo" // nombre del drawable
//        )
        return CatalogoCompleto(
            familias = emptyList(),
            logo = ""
        )
    }

    override suspend fun getProductoParaCatalogo(productoId: Long): ProductoCatalogo? {
//        val producto = productoDao.getById(productoId) ?: return null
//        val atributos = atributoDao.getByProductoId(productoId)
//        return CatalogoMapper.mapProductoToProductoCatalogo(producto, atributos)
        return null
    }

    override suspend fun getProductosPorCategoria(categoriaId: Long): List<ProductoCatalogo> {
//        val subcategorias = subcategoriaDao.getByCategoriaId(categoriaId)
//        return subcategorias.flatMap { subcat ->
//            val productos = productoDao.getBySubcategoriaId(subcat.id)
//            productos.map { producto ->
//                val atributos = atributoDao.getByProductoId(producto.id)
//                CatalogoMapper.mapProductoToProductoCatalogo(producto, atributos)
//            }
//        }
        return emptyList()
    }
}