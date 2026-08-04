package com.passioagogo.market.data.customers

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ Dominio ============

data class Customer(
    val id: String,
    val profileId: String?,
    val nombre: String,
    val telefono: String?,
    val email: String?,
    val notas: String?,
    val activo: Boolean,
)

data class CustomerDraft(
    val nombre: String,
    val telefono: String? = null,
    val email: String? = null,
    val notas: String? = null,
)

data class ShippingAddress(
    val id: String,
    val customerId: String,
    val alias: String?,
    val nombreReceptor: String,
    val telefono: String,
    val calle: String,
    val numero: String?,
    val colonia: String?,
    val ciudad: String,
    val estado: String,
    val codigoPostal: String,
    val referencias: String?,
    val esPredeterminada: Boolean,
    val activo: Boolean,
) {
    val resumen: String
        get() = listOfNotNull(
            alias,
            "$calle ${numero.orEmpty()}".trim(),
            colonia,
            "$ciudad, $estado",
        ).joinToString(" · ")
}

data class AddressDraft(
    val customerId: String,
    val alias: String? = null,
    val nombreReceptor: String,
    val telefono: String,
    val calle: String,
    val numero: String? = null,
    val colonia: String? = null,
    val ciudad: String,
    val estado: String,
    val codigoPostal: String,
    val referencias: String? = null,
)

interface CustomerRepository {
    suspend fun getCustomers(includeInactive: Boolean = false): DataResult<List<Customer>>
    suspend fun createCustomer(draft: CustomerDraft): DataResult<Customer>
    suspend fun updateCustomer(customer: Customer): DataResult<Customer>
    suspend fun getAddresses(customerId: String): DataResult<List<ShippingAddress>>
    suspend fun createAddress(draft: AddressDraft): DataResult<ShippingAddress>
    /** Borrado lógico: los pedidos históricos siguen referenciándola. */
    suspend fun deactivateAddress(addressId: String): DataResult<Unit>
}

// ============ DTOs ============

@Serializable
data class CustomerDto(
    val id: String,
    @SerialName("profile_id") val profileId: String? = null,
    val nombre: String,
    val telefono: String? = null,
    val email: String? = null,
    val notas: String? = null,
    val activo: Boolean = true,
) {
    fun toDomain() = Customer(
        id = id, profileId = profileId, nombre = nombre,
        telefono = telefono, email = email, notas = notas, activo = activo,
    )
}

@Serializable
data class NewCustomerDto(
    val nombre: String,
    val telefono: String? = null,
    val email: String? = null,
    val notas: String? = null,
)

@Serializable
data class ShippingAddressDto(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    val alias: String? = null,
    @SerialName("nombre_receptor") val nombreReceptor: String,
    val telefono: String,
    val calle: String,
    val numero: String? = null,
    val colonia: String? = null,
    val ciudad: String,
    val estado: String,
    @SerialName("codigo_postal") val codigoPostal: String,
    val referencias: String? = null,
    @SerialName("es_predeterminada") val esPredeterminada: Boolean = false,
    val activo: Boolean = true,
) {
    fun toDomain() = ShippingAddress(
        id = id, customerId = customerId, alias = alias,
        nombreReceptor = nombreReceptor, telefono = telefono, calle = calle,
        numero = numero, colonia = colonia, ciudad = ciudad, estado = estado,
        codigoPostal = codigoPostal, referencias = referencias,
        esPredeterminada = esPredeterminada, activo = activo,
    )
}

@Serializable
data class NewShippingAddressDto(
    @SerialName("customer_id") val customerId: String,
    val alias: String? = null,
    @SerialName("nombre_receptor") val nombreReceptor: String,
    val telefono: String,
    val calle: String,
    val numero: String? = null,
    val colonia: String? = null,
    val ciudad: String,
    val estado: String,
    @SerialName("codigo_postal") val codigoPostal: String,
    val referencias: String? = null,
)

// ============ Implementación ============

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CustomerRepository {

    private companion object {
        const val CUSTOMERS = "customers"
        const val ADDRESSES = "shipping_addresses"
    }

    override suspend fun getCustomers(includeInactive: Boolean): DataResult<List<Customer>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(CUSTOMERS).select {
                    if (!includeInactive) filter { eq("activo", true) }
                    order("nombre", Order.ASCENDING)
                }.decodeList<CustomerDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createCustomer(draft: CustomerDraft): DataResult<Customer> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(CUSTOMERS).insert(
                    NewCustomerDto(
                        nombre = draft.nombre,
                        telefono = draft.telefono,
                        email = draft.email,
                        notas = draft.notas,
                    )
                ) { select() }.decodeSingle<CustomerDto>()
            }.map { it.toDomain() }
        }

    override suspend fun updateCustomer(customer: Customer): DataResult<Customer> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(CUSTOMERS).update({
                    set("nombre", customer.nombre)
                    set("telefono", customer.telefono)
                    set("email", customer.email)
                    set("notas", customer.notas)
                    set("activo", customer.activo)
                }) {
                    select()
                    filter { eq("id", customer.id) }
                }.decodeSingle<CustomerDto>()
            }.map { it.toDomain() }
        }

    override suspend fun getAddresses(customerId: String): DataResult<List<ShippingAddress>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(ADDRESSES).select {
                    filter {
                        eq("customer_id", customerId)
                        eq("activo", true)
                    }
                    order("created_at", Order.DESCENDING)
                }.decodeList<ShippingAddressDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createAddress(draft: AddressDraft): DataResult<ShippingAddress> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(ADDRESSES).insert(
                    NewShippingAddressDto(
                        customerId = draft.customerId,
                        alias = draft.alias,
                        nombreReceptor = draft.nombreReceptor,
                        telefono = draft.telefono,
                        calle = draft.calle,
                        numero = draft.numero,
                        colonia = draft.colonia,
                        ciudad = draft.ciudad,
                        estado = draft.estado,
                        codigoPostal = draft.codigoPostal,
                        referencias = draft.referencias,
                    )
                ) { select() }.decodeSingle<ShippingAddressDto>()
            }.map { it.toDomain() }
        }

    override suspend fun deactivateAddress(addressId: String): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(ADDRESSES).update({
                    set("activo", false)
                    set("es_predeterminada", false)
                }) { filter { eq("id", addressId) } }
                Unit
            }
        }
}
