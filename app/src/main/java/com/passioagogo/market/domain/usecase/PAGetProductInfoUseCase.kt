package com.passioagogo.market.domain.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.passioagogo.market.ui.utils.PAConstants.TAG_PG
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.ui.utils.PAConstants.COLLECTION_PRODUCTS
import javax.inject.Inject

class PAGetProductInfoUseCase @Inject constructor() {
    val bd = Firebase.firestore
    operator fun invoke(
        field: String,
        value: String,
        limit: Long?= null,
        lastDocument: DocumentSnapshot?= null,
        response: (PADomainState<QuerySnapshot>) -> Unit,
    ){
        val query = bd.collection(COLLECTION_PRODUCTS)
            .whereEqualTo(field,value)
            .limit(limit ?: 15)

        lastDocument?.let {
            query
                .startAfter(lastDocument)
                .get()
                .addOnSuccessListener{  result ->
                    response(PADomainState.Success(result))
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG_PG,"$exception")

                }
        } ?: run {
            query
                .get()
                .addOnSuccessListener{  result ->
                    response(PADomainState.Success(result))
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG_PG,"$exception")

                }
        }
    }
}
