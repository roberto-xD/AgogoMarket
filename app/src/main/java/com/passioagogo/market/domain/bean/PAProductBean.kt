package com.passioagogo.market.domain.bean

data class PAProductBean(
    var id: String? = null,
    val sku: String? = null,
    val title: String? = null,
    val store: String? = null,
    val description: String? = null,
    val image: String? = null,
    val category: String? = null,
    val link: String? = null,
    val isActive: Boolean? = null,
    val units : String? = null,
    val actual_stock: String? = null,
    val initial_stock: String? = null,
    val reposition_quantity: String? = null,
    val price: PAPriceBean? = null,
    )

data class PAPriceBean(
    val public: String? = null,
    val discount: String? = null,
    val supplier: String? = null,
    val hasOffer: Boolean? = null,
)

fun PAProductBean.update(
    sku: String? = null,
    title: String? = null,
    store: String? = null,
    description: String? = null,
    image: String? = null,
    category: String? = null,
    actual_stock: String? = null,
    initial_stock: String? = null,
    reposition_quantity: String? = null,
    units: String? = null,
    link: String? = null,
    isActive: Boolean? = null,
): PAProductBean {
    return this.copy(
        id = id,
        sku = sku,
        title = title, 
        store = store, 
        description = description, 
        image = image, 
        category = category, 
        actual_stock = actual_stock,
        initial_stock = initial_stock,
        reposition_quantity = reposition_quantity,
        units = units,
        link = link,
        isActive = isActive,
    )
}

fun PAProductBean.updatePrice(
    public: String? = null,
    discount: String? = null,
    supplier: String? = null,
    hasOffer: Boolean? = null,
): PAProductBean{
    val price = this.price?.copy(
        public = public,
        discount = discount,
        supplier = supplier,
        hasOffer = hasOffer,
    )
    return this.copy(
        price = price
    )
}