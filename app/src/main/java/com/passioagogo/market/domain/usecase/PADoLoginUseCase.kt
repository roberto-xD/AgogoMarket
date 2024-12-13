package com.passioagogo.market.domain.usecase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import javax.inject.Inject

class PADoLoginUseCase @Inject constructor() {
    val bd = Firebase.auth
    operator fun invoke(
        email: String,
        password: String,
        success: (FirebaseUser?)-> Unit,
        fail: (error: String?)-> Unit
    ){
        bd.signInWithEmailAndPassword(
            email,password
        ).addOnCompleteListener {
            success(it.result.user)
        }.addOnFailureListener {
            fail(it.message)
        }
    }
}