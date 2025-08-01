package com.passioagogo.market.models

import com.google.common.truth.Truth
import com.passioagogo.market.domain.state.PADomainState
import com.passioagogo.market.domain.state.onError
import com.passioagogo.market.domain.state.onSuccess
import org.junit.Test

class ResultTest {

    @Test
    fun `onSuccess debe ejecutar accion cuando es Success`() {
        // Given
        var executed = false
        val result: PADomainState<String> = PADomainState.Success("test")

        // When
        result.onSuccess { executed = true }

        // Then
        Truth.assertThat(executed).isTrue()
    }

    @Test
    fun `onSuccess no debe ejecutar accion cuando es Error`() {
        // Given
        var executed = false
        val result: PADomainState<String> = PADomainState.Error(RuntimeException("test"))

        // When
        result.onSuccess { executed = true }

        // Then
        Truth.assertThat(executed).isFalse()
    }

    @Test
    fun `onError debe ejecutar accion cuando es Error`() {
        // Given
        var executed = false
        val exception = RuntimeException("test")
        val result: PADomainState<String> = PADomainState.Error(exception)

        // When
        result.onError { executed = true }

        // Then
        Truth.assertThat(executed).isTrue()
    }

    @Test
    fun `onError no debe ejecutar accion cuando es Success`() {
        // Given
        var executed = false
        val result: PADomainState<String> = PADomainState.Success("test")

        // When
        result.onError { executed = true }

        // Then
        Truth.assertThat(executed).isFalse()
    }
}