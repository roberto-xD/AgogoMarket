package com.passioagogo.market.domain.promotions

import com.passioagogo.market.core.result.DataResult
import com.passioagogo.market.domain.common.PromotionType

data class Promotion(
    val id: String,
    val nombre: String,
    val tipo: PromotionType,
    val valor: Double,
    val fechaInicio: String,
    val fechaFin: String,
    val activo: Boolean,
    val notas: String?,
    val targets: List<PromotionTarget>,
)

/** Exactamente UNO de los tres ids es no-nulo (CHECK del esquema). */
data class PromotionTarget(
    val id: String,
    val categoryId: String?,
    val productId: String?,
    val variantId: String?,
)

data class TargetDraft(
    val categoryId: String? = null,
    val productId: String? = null,
    val variantId: String? = null,
)

data class PromotionDraft(
    val nombre: String,
    val tipo: PromotionType,
    val valor: Double,
    /** ISO-8601 (timestamptz). */
    val fechaInicio: String,
    val fechaFin: String,
    val notas: String? = null,
    val targets: List<TargetDraft>,
)

/**
 * Promociones (solo admin escribe, RLS decide). El no-traslape lo
 * valida el servidor (fn_check_promo_overlap): sus errores llegan
 * como DataError.Business con el nombre de la promo en conflicto.
 */
interface PromotionRepository {
    suspend fun getPromotions(includeInactive: Boolean = true): DataResult<List<Promotion>>
    suspend fun createPromotion(draft: PromotionDraft): DataResult<Promotion>
    suspend fun updatePromotion(promotion: Promotion): DataResult<Promotion>
    suspend fun addTarget(promotionId: String, target: TargetDraft): DataResult<Unit>
    suspend fun removeTarget(targetId: String): DataResult<Unit>
}
