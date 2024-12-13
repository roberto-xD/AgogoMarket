package com.passioagogo.market.data.state

sealed class PARemoteState<T>(
    val data: T? = null,
    val error: String? = null
) {
    class Success<T>(data: T?) : PARemoteState<T>(data = data)
    class Error<T>(error: String?) : PARemoteState<T>(error = error)
}