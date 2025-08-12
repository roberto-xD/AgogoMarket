package com.passioagogo.market.domain.usecase.base
import com.passioagogo.market.domain.state.PADomainState

abstract class NoParamsUseCase<R> {
    suspend operator fun invoke(): PADomainState<R> {
        return try {
            execute().let { result ->
                PADomainState.Success(result)
            }
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): R
}