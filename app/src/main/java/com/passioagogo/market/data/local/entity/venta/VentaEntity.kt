package com.passioagogo.market.data.local.entity.venta

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.passioagogo.market.domain.model.venta.EstadoVenta
import com.passioagogo.market.domain.model.venta.ProductoVenta
import com.passioagogo.market.domain.model.venta.Venta
import com.passioagogo.market.presentation.view.components.ClienteModel

@Entity(
    tableName = "ventas",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class VentaEntity(
    @PrimaryKey()
    val id: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val clienteNombre: String = "Público en general",
    val clienteTelefono: String = "",
    val clienteDireccion: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaLiquidacion: Long? = null,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val instruccionesEntrega: String = "",
    val estado: String = "PENDIENTE", // PENDIENTE, LIQUIDADA, CANCELADA
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false
)

fun VentaEntity.toDomain(productos: List<ProductoVenta> = emptyList()) = Venta(
    id = id,
    clienteModel = ClienteModel(
        nombre = clienteNombre,
        telefono = clienteTelefono,
        direccion = clienteDireccion
    ),
    fechaCreacion = fechaCreacion,
    fechaLiquidacion = fechaLiquidacion,
    total = total,
    metodoPago = metodoPago,
    instruccionesEntrega = instruccionesEntrega,
    estado = EstadoVenta.valueOf(estado),
    productos = productos
)