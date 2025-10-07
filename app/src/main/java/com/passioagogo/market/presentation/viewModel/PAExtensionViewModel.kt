package com.passioagogo.market.presentation.viewModel

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BackPressHandler(private val delayMillis: Long = 2000L) {
    private var backPressedOnce = false
    private var resetJob: Job? = null

    fun onBackPressed(
        scope: kotlinx.coroutines.CoroutineScope,
        onFirstPress: () -> Unit,
        onSecondPress: () -> Unit
    ) {
        if (backPressedOnce) {
            resetJob?.cancel()
            onSecondPress()
        } else {
            backPressedOnce = true
            onFirstPress()

            resetJob?.cancel()
            resetJob = scope.launch {
                delay(delayMillis)
                backPressedOnce = false
            }
        }
    }

    fun reset() {
        resetJob?.cancel()
        backPressedOnce = false
    }
}