package com.passioagogo.market.data.promotions

import com.passioagogo.market.core.di.IoDispatcher
import com.passioagogo.market.core.result.DataError
import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.core.result.map
import com.passioagogo.market.core.result.safeSupabaseCall
import com.passioagogo.market.domain.common.PromotionType
import com.passioagogo.market.domain.promotions.Promotion
import com.passioagogo.market.domain.promotions.PromotionDraft
import com.passioagogo.market.domain.promotions.PromotionRepository
import com.passioagogo.market.domain.promotions.PromotionTarget
import com.passioagogo.market.domain.promotions.TargetDraft
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromotionDto(
    val id: String,
    val nombre: String,
    val tipo: PromotionType,
    val valor: Double,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String,
    val activo: Boolean = true,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
    @SerialName("promotion_targets") val targets: List<PromotionTargetDto> = emptyList(),
) {
    fun toDomain() = Promotion(
        id = id, nombre = nombre, tipo = tipo, valor = valor,
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        activo = activo, notas = notas,
        targets = targets.map {
            PromotionTarget(
                id = it.id, categoryId = it.categoryId,
                productId = it.productId, variantId = it.variantId,
            )
        },
    )
}

@Serializable
data class PromotionTargetDto(
    val id: String,
    @SerialName("promotion_id") val promotionId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("variant_id") val variantId: String? = null,
)

@Serializable
data class NewPromotionDto(
    val nombre: String,
    val tipo: PromotionType,
    val valor: Double,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String,
    val notas: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class NewPromotionTargetDto(
    @SerialName("promotion_id") val promotionId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("variant_id") val variantId: String? = null,
)

@Singleton
class PromotionRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PromotionRepository {

    private companion object {
        const val PROMOTIONS = "promotions"
        const val TARGETS = "promotion_targets"
        val PROMO_COLUMNS = Columns.raw("*, promotion_targets(*)")
    }

    override suspend fun getPromotions(includeInactive: Boolean): DataResult<List<Promotion>> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PROMOTIONS).select(PROMO_COLUMNS) {
                    if (!includeInactive) filter { eq("activo", true) }
                    order("fecha_inicio", Order.DESCENDING)
                }.decodeList<PromotionDto>()
            }.map { list -> list.map { it.toDomain() } }
        }

    override suspend fun createPromotion(draft: PromotionDraft): DataResult<Promotion> =
        withContext(io) {
            val userId = auth.currentUserOrNull()?.id
                ?: return@withContext DataResult.Error(DataError.Unauthorized)
            if (draft.targets.isEmpty()) {
                return@withContext DataResult.Error(
                    DataError.Business("La promoción requiere al menos un objetivo")
                )
            }

            val created = safeSupabaseCall {
                postgrest.from(PROMOTIONS).insert(
                    NewPromotionDto(
                        nombre = draft.nombre,
                        tipo = draft.tipo,
                        valor = draft.valor,
                        fechaInicio = draft.fechaInicio,
                        fechaFin = draft.fechaFin,
                        notas = draft.notas,
                        createdBy = userId,
                    )
                ) { select() }.decodeSingle<PromotionDto>()
            }
            val promo = when (created) {
                is DataResult.Error -> return@withContext created
                is DataResult.Success -> created.data
            }

            // El trigger de traslape valida aquí; si rechaza, la cabecera
            // sin targets no cubre nada: se elimina para no dejar basura.
            val targetsResult = safeSupabaseCall {
                postgrest.from(TARGETS).insert(
                    draft.targets.map {
                        NewPromotionTargetDto(
                            promotionId = promo.id,
                            categoryId = it.categoryId,
                            productId = it.productId,
                            variantId = it.variantId,
                        )
                    }
                )
            }
            when (targetsResult) {
                is DataResult.Error -> {
                    safeSupabaseCall {
                        postgrest.from(PROMOTIONS).delete { filter { eq("id", promo.id) } }
                    }
                    targetsResult
                }
                is DataResult.Success -> getPromotion(promo.id)
            }
        }

    override suspend fun updatePromotion(promotion: Promotion): DataResult<Promotion> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(PROMOTIONS).update({
                    set("nombre", promotion.nombre)
                    set("tipo", promotion.tipo)
                    set("valor", promotion.valor)
                    set("fecha_inicio", promotion.fechaInicio)
                    set("fecha_fin", promotion.fechaFin)
                    set("activo", promotion.activo)
                    set("notas", promotion.notas)
                }) {
                    select(PROMO_COLUMNS)
                    filter { eq("id", promotion.id) }
                }.decodeSingle<PromotionDto>()
            }.map { it.toDomain() }
        }

    override suspend fun addTarget(promotionId: String, target: TargetDraft): DataResult<Unit> =
        withContext(io) {
            safeSupabaseCall {
                postgrest.from(TARGETS).insert(
                    NewPromotionTargetDto(
                        promotionId = promotionId,
                        categoryId = target.categoryId,
                        productId = target.productId,
                        variantId = target.variantId,
                    )
                )
                Unit
            }
        }

    override suspend fun removeTarget(targetId: String): DataResult<Unit> = withContext(io) {
        safeSupabaseCall {
            postgrest.from(TARGETS).delete { filter { eq("id", targetId) } }
            Unit
        }
    }

    private suspend fun getPromotion(id: String): DataResult<Promotion> =
        safeSupabaseCall {
            postgrest.from(PROMOTIONS).select(PROMO_COLUMNS) {
                filter { eq("id", id) }
            }.decodeSingle<PromotionDto>()
        }.map { it.toDomain() }
}
