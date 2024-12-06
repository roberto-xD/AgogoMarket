package com.passioagogo.market.domain.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.passioagogo.market.domain.PAConstants.COLLECTION_CONSUMABLES
import com.passioagogo.market.domain.PAConstants.COLLECTION_PRODUCTS
import com.passioagogo.market.domain.bean.PAProductBean
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PAGetProductInfoUseCase @Inject constructor(

) {
    val bd = Firebase.firestore
    operator fun invoke(): Flow<PADomainState<PAProductBean>> = flow{
        bd.collection(COLLECTION_PRODUCTS)
            .document(COLLECTION_CONSUMABLES)
            .get()
            .addOnSuccessListener{  result ->
                Log.i("tag","$result")
            }
            .addOnFailureListener { exception ->
                Log.i("tag","$exception")
            }
    }
}