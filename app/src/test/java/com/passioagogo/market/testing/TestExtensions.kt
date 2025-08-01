package com.passioagogo.market.testing

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.testIn
import com.passioagogo.market.domain.state.PADomainState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T> PADomainState<T>.shouldBeSuccess(): T {
    return when (this) {
        is PADomainState.Success -> this.data
        is PADomainState.Error -> throw AssertionError("Expected Success but was Error: ${this.exception.message}")
        is PADomainState.Loading -> throw AssertionError("Expected Success but was Loading")
    }
}

// Extensión para verificar Result.Error
fun <T> PADomainState<T>.shouldBeError(): Throwable {
    return when (this) {
        is PADomainState.Success -> throw AssertionError("Expected Error but was Success: ${this.data}")
        is PADomainState.Error -> this.exception
        is PADomainState.Loading -> throw AssertionError("Expected Error but was Loading")
    }
}

// Extensión para verificar que Result es Loading
fun <T> PADomainState<T>.shouldBeLoading() {
    if (this !is PADomainState.Loading) {
        throw AssertionError("Expected Loading but was ${this::class.simpleName}")
    }
}

// Extensión para Flow testing con Turbine
suspend fun <T> Flow<T>.test(
    timeout: Duration = 1.seconds,
    validate: suspend ReceiveTurbine<T>.() -> Unit
) {
    testIn(
        timeout = timeout,
        scope = this@test as CoroutineScope,
    ).validate()
}