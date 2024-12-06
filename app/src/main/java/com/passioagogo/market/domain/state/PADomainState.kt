package com.passioagogo.market.domain.state

sealed class PADomainState<T>(
    val data: T? = null,
    val error: String? = null,
    val isLoading: Boolean? = null
){
    class Success<T>(data: T?) : PADomainState<T>(data = data)
    class Error<T>(error: String?) : PADomainState<T>(error = error)
    class Loading<T>(isLoading: Boolean?) : PADomainState<T>(isLoading = isLoading)
}