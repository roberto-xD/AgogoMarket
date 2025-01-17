package com.passioagogo.market.domain.usecase


import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.passioagogo.market.domain.PAConstants.COLLECTION_PRODUCTS
import com.passioagogo.market.domain.PAConstants.TAG_PG
import com.passioagogo.market.domain.state.PADomainState
import javax.inject.Inject

class PASearchProductInfoUseCase @Inject constructor() {
    val bd = Firebase.firestore
    operator fun invoke(
        field: String,
        value: String,
        limit: Long? = null,
        response: (PADomainState<QuerySnapshot>) -> Unit,
    ) {
        response(PADomainState.Loading())
        bd.collection(COLLECTION_PRODUCTS)
            .whereArrayContains(field, value)
            .limit(limit ?: 15)
            .get()
            .addOnSuccessListener{  result ->
                response(PADomainState.Success(result))
            }
            .addOnFailureListener { exception ->
                Log.i(TAG_PG,"$exception")
                response(PADomainState.Error(exception.message))
            }
    }
}