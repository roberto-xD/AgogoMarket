package com.passioagogo.market.domain.state

sealed class PADomainState<out T>{
    data class Success<out T>(val data: T) : PADomainState<T>()
    data class Error(val exception: Throwable) : PADomainState<Nothing>()
    data object Loading : PADomainState<Nothing>()
}

inline fun <T> PADomainState<T>.onSuccess(action: (value: T) -> Unit): PADomainState<T> {
    if (this is PADomainState.Success) action(data)
    return this
}

inline fun <T> PADomainState<T>.onError(action: (exception: Throwable) -> Unit): PADomainState<T> {
    if (this is PADomainState.Error) action(exception)
    return this
}