package com.passioagogo.market.data.local
import PADataConstants.NAME_TABLE
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.passioagogo.market.data.local.converters.Converters
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.dao.HistorialPrecioDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoFamiliaDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.data.local.dao.TipoAtributoDao
import com.passioagogo.market.data.local.dao.VentaDao
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.data.local.entity.dinamics.TipoAtributoEntity
import com.passioagogo.market.data.local.entity.relation.ProductoCategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoFamiliaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.data.local.entity.relation.ProductoSubcategoriaEntity
import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import com.passioagogo.market.data.local.entity.venta.VentaEntity
import com.passioagogo.market.data.local.entity.venta.VentaProductoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FamiliaEntity::class,
        CategoriaEntity::class,
        SubcategoriaEntity::class,
        ProveedorEntity::class,
        ProductoEntity::class,
        ProductoFamiliaEntity::class,
        ProductoCategoriaEntity::class,
        ProductoSubcategoriaEntity::class,
        ProductoProveedorEntity::class,
        TipoAtributoEntity::class,
        ProductoAtributoEntity::class,
        ProductoImagenEntity::class,
        HistorialPrecioEntity::class,
        VentaEntity::class,
        VentaProductoEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class InventarioDatabase : RoomDatabase() {

    // DAOs abstractos
    abstract fun familiaDao(): FamiliaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun subcategoriaDao(): SubcategoriaDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun productoDao(): ProductoDao
    abstract fun productoFamiliaDao(): ProductoFamiliaDao
    abstract fun productoCategoriaDao(): ProductoCategoriaDao
    abstract fun productoSubcategoriaDao(): ProductoSubcategoriaDao
    abstract fun productoProveedorDao(): ProductoProveedorDao
    abstract fun tipoAtributoDao(): TipoAtributoDao
    abstract fun productoAtributoDao(): ProductoAtributoDao
    abstract fun productoImagenDao(): ProductoImagenDao
    abstract fun historialPrecioDao(): HistorialPrecioDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: InventarioDatabase? = null

        fun getDatabase(context: Context): InventarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    NAME_TABLE
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Ejecutar en corrutina para insertar datos iniciales
                CoroutineScope(Dispatchers.IO).launch {
                    insertarDatosIniciales()
                }
            }

            private suspend fun insertarDatosIniciales() {
                val database = INSTANCE ?: return

                // Insertar familias iniciales
                val familiasIniciales = listOf(
                    FamiliaEntity(nombre = "juguetes_adultos", descripcion = "Juguetes para adultos"),
                    FamiliaEntity(nombre = "consumibles", descripcion = "Productos consumibles"),
                    FamiliaEntity(nombre = "lenceria", descripcion = "Lencería íntima")
                )

                familiasIniciales.forEach { familia ->
                    database.familiaDao().insertarFamilia(familia)
                }

                // Insertar tipos de atributos comunes
                val tiposAtributosIniciales = listOf(
                    TipoAtributoEntity(nombre = "marca", tipoDato = "TEXT"),
                    TipoAtributoEntity(nombre = "material", tipoDato = "TEXT"),
                    TipoAtributoEntity(nombre = "base", tipoDato = "TEXT"),
                    TipoAtributoEntity(nombre = "sabor", tipoDato = "TEXT"),
                    TipoAtributoEntity(nombre = "recargable", tipoDato = "BOOLEAN"),
                    TipoAtributoEntity(nombre = "talla", tipoDato = "TEXT"),
                    TipoAtributoEntity(nombre = "peso", tipoDato = "NUMBER"),
                    TipoAtributoEntity(nombre = "dimensiones", tipoDato = "TEXT")
                )

                tiposAtributosIniciales.forEach { tipo ->
                    database.tipoAtributoDao().insertarTipoAtributo(tipo)
                }
            }
        }
    }
}

// Migración de versión 1 a 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Agregar campos a familias
        database.execSQL("ALTER TABLE familias ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE familias ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE familias ADD COLUMN updatedAt INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE familias ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE familias ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE familias ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_familias_remoteId ON familias(remoteId)")

        // Agregar campos a categorias
        database.execSQL("ALTER TABLE categorias ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE categorias ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE categorias ADD COLUMN familiaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE categorias ADD COLUMN updatedAt INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE categorias ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE categorias ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE categorias ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categorias_remoteId ON categorias(remoteId)")

        // Agregar campos a subcategorias
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN categoriaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN updatedAt INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE subcategorias ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_subcategorias_remoteId ON subcategorias(remoteId)")

        // Agregar campos a proveedores
        database.execSQL("ALTER TABLE proveedores ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE proveedores ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE proveedores ADD COLUMN updatedAt INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE proveedores ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE proveedores ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE proveedores ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_proveedores_remoteId ON proveedores(remoteId)")

        // Agregar campos a productos
        database.execSQL("ALTER TABLE productos ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE productos ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE productos ADD COLUMN proveedorPrincipalRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE productos ADD COLUMN updatedAt INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE productos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE productos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE productos ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_productos_remoteId ON productos(remoteId)")

        // Agregar campos a tipos_atributos
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN fechaCreacion INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE tipos_atributos ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tipos_atributos_remoteId ON tipos_atributos(remoteId)")

        // Agregar campos a producto_atributos
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN tipoAtributoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_atributos ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_producto_atributos_remoteId ON producto_atributos(remoteId)")

        // Agregar campos a historial_precios
        database.execSQL("ALTER TABLE historial_precios ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE historial_precios ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE historial_precios ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE historial_precios ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE historial_precios ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_historial_precios_remoteId ON historial_precios(remoteId)")

        // Agregar campos a producto_imagenes
        database.execSQL("ALTER TABLE producto_imagenes ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_imagenes ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_imagenes ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_imagenes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_imagenes ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_producto_imagenes_remoteId ON producto_imagenes(remoteId)")

        // Agregar campos a producto_familia
        database.execSQL("ALTER TABLE producto_familia ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_familia ADD COLUMN familiaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_familia ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_familia ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")

        // Agregar campos a producto_categorias
        database.execSQL("ALTER TABLE producto_categorias ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_categorias ADD COLUMN categoriaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_categorias ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_categorias ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")

        // Agregar campos a producto_subcategorias
        database.execSQL("ALTER TABLE producto_subcategorias ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_subcategorias ADD COLUMN subcategoriaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_subcategorias ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_subcategorias ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")

        // Agregar campos a producto_proveedores
        database.execSQL("ALTER TABLE producto_proveedores ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_proveedores ADD COLUMN proveedorRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE producto_proveedores ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE producto_proveedores ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")

        // Agregar campos a ventas
        database.execSQL("ALTER TABLE ventas ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ventas ADD COLUMN userId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ventas ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ventas ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ventas ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ventas_remoteId ON ventas(remoteId)")

        // Agregar campos a venta_productos
        database.execSQL("ALTER TABLE venta_productos ADD COLUMN remoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE venta_productos ADD COLUMN ventaRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE venta_productos ADD COLUMN productoRemoteId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE venta_productos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE venta_productos ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_venta_productos_remoteId ON venta_productos(remoteId)")
    }
}