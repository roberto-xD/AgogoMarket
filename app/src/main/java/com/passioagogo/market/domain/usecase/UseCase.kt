package com.passioagogo.market.domain.usecase
import com.passioagogo.market.domain.state.PADomainState

abstract class UseCase<in P, R> {
    suspend operator fun invoke(parameters: P): PADomainState<R> {
        return try {
            execute(parameters).let { result ->
                PADomainState.Success(result)
            }
        } catch (e: Exception) {
            PADomainState.Error(e)
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}