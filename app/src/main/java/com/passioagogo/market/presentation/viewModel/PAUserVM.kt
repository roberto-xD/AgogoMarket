package com.passioagogo.market.presentation.viewModel

import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.usecase.PADoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PAUserVM @Inject constructor(
    private val doLogin: PADoLoginUseCase
) : ViewModel() {
    private val _user = MutableStateFlow<PADomainState<FirebaseUser>?>(null)
    val user : StateFlow<PADomainState<FirebaseUser>?> = _user
    fun getSession(
        email: String,
        password: String
    ){
        doLogin(
            email = email,
            password = password,
            success = {
                _user.value = PADomainState.Success(it)
            },
            fail = {
                _user.value = PADomainState.Error(it)
            }
        )
    }
}