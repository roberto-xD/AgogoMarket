package com.passioagogo.market.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.passioagogo.market.data.local.converters.Converters
import com.passioagogo.market.data.local.dao.CategoriaDao
import com.passioagogo.market.data.local.dao.FamiliaDao
import com.passioagogo.market.data.local.dao.HistorialPrecioDao
import com.passioagogo.market.data.local.dao.ProductoAtributoDao
import com.passioagogo.market.data.local.dao.ProductoCategoriaDao
import com.passioagogo.market.data.local.dao.ProductoDao
import com.passioagogo.market.data.local.dao.ProductoImagenDao
import com.passioagogo.market.data.local.dao.ProductoProveedorDao
import com.passioagogo.market.data.local.dao.ProductoSubcategoriaDao
import com.passioagogo.market.data.local.dao.ProveedorDao
import com.passioagogo.market.data.local.dao.SubcategoriaDao
import com.passioagogo.market.data.local.dao.TipoAtributoDao
import com.passioagogo.market.data.local.entity.base.CategoriaEntity
import com.passioagogo.market.data.local.entity.base.FamiliaEntity
import com.passioagogo.market.data.local.entity.base.ProductoEntity
import com.passioagogo.market.data.local.entity.base.ProveedorEntity
import com.passioagogo.market.data.local.entity.base.SubcategoriaEntity
import com.passioagogo.market.data.local.entity.dinamics.ProductoAtributoEntity
import com.passioagogo.market.data.local.entity.dinamics.TipoAtributoEntity
import com.passioagogo.market.data.local.entity.relation.ProductoCategoriaEntity
import com.passioagogo.market.data.local.entity.relation.ProductoProveedorEntity
import com.passioagogo.market.data.local.entity.relation.ProductoSubcategoriaEntity
import com.passioagogo.market.data.local.entity.utils.HistorialPrecioEntity
import com.passioagogo.market.data.local.entity.utils.ProductoImagenEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [
        FamiliaEntity::class,
        CategoriaEntity::class,
        SubcategoriaEntity::class,
        ProveedorEntity::class,
        ProductoEntity::class,
        ProductoCategoriaEntity::class,
        ProductoSubcategoriaEntity::class,
        ProductoProveedorEntity::class,
        TipoAtributoEntity::class,
        ProductoAtributoEntity::class,
        ProductoImagenEntity::class,
        HistorialPrecioEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InventarioDatabase : RoomDatabase() {

    // DAOs abstractos
    abstract fun familiaDao(): FamiliaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun subcategoriaDao(): SubcategoriaDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun productoDao(): ProductoDao
    abstract fun productoCategoriaDao(): ProductoCategoriaDao
    abstract fun productoSubcategoriaDao(): ProductoSubcategoriaDao
    abstract fun productoProveedorDao(): ProductoProveedorDao
    abstract fun tipoAtributoDao(): TipoAtributoDao
    abstract fun productoAtributoDao(): ProductoAtributoDao
    abstract fun productoImagenDao(): ProductoImagenDao
    abstract fun historialPrecioDao(): HistorialPrecioDao

    companion object {
        @Volatile
        private var INSTANCE: InventarioDatabase? = null

        fun getDatabase(context: Context): InventarioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    "inventario_database"
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
