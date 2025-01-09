package com.passioagogo.market.domain.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.passioagogo.market.domain.PAConstants.COLLECTION_PRODUCTS
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject

class PAGetProductInfoUseCase @Inject constructor() {
    val bd = Firebase.firestore
    operator fun invoke(
        field: String,
        value: String,
        limit: Long?,
        response: (PADomainState<List<PAProductBean>>) -> Unit,
    ){
        response(PADomainState.Loading())
        bd.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(field,value)
            .limit(limit ?: 10)
            .get()
            .addOnSuccessListener{  result ->
                val plop = result.map {
                    it.toObject(PAProductBean::class.java).apply { id = it.id }
                }
                Log.i("tag success", "${plop}")
                response(PADomainState.Success(plop))
            }
            .addOnFailureListener { exception ->
                Log.i("tag fail","$exception")
                response(PADomainState.Error(exception.message))
            }
    }
}
