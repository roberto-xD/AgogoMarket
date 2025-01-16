package com.passioagogo.market.domain.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.passioagogo.market.domain.PAConstants.COLLECTION_PRODUCTS
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject

class PANewProductUseCase @Inject constructor() {
    val bd = Firebase.firestore
    operator fun invoke(
        infoProduct : PAProductBean,
        response: (PADomainState<PAProductBean>) -> Unit,
    ){
        response(PADomainState.Loading())
        val newQuery = bd.collection(COLLECTION_PRODUCTS)
            .document()
        val productWithId = infoProduct.copy(
            id = newQuery.id,
            store = "sexshop",
            isActive = true
        )
        newQuery
            .set(productWithId)
            .addOnSuccessListener {
                response(PADomainState.Success(productWithId))
            }
            .addOnFailureListener { exception ->
                Log.i("tag fail","$exception")
                response(PADomainState.Error(exception.message))
            }
    }
}