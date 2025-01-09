package com.passioagogo.market.domain.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.passioagogo.market.domain.PAConstants.COLLECTION_CONSUMABLES
import com.passioagogo.market.domain.PAConstants.COLLECTION_PRODUCTS
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PASaveProductInfoUseCase @Inject constructor() {
    val bd = Firebase.firestore
    operator fun invoke(
        infoProduct : PAProductBean,
        response: (PADomainState<PAProductBean>) -> Unit,
    ) {
        response(PADomainState.Loading())
        bd.collection(COLLECTION_PRODUCTS)
            .document(infoProduct.id)
            .set(infoProduct, SetOptions.merge())
            .addOnSuccessListener {
                response(PADomainState.Success(infoProduct))
            }
            .addOnFailureListener { exception ->
                Log.i("tag fail","$exception")
                response(PADomainState.Error(exception.message))
            }
    }
}