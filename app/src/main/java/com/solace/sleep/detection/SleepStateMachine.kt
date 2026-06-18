package com.solace.sleep.detection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.time.Instant

enum class SleepState {
    IDLE,
    WATCHING,
    PRE_SLEEP,
    SLEEPING,
    POST_SLEEP,
    FINALIZED
}

data class SleepStateContext(
    val state: SleepState = SleepState.IDLE,
    val watchingStartedAt: Instant? = null,
    val preSleepStartedAt: Instant? = null,
    val sleepOnsetAt: Instant? = null,
    val postSleepStartedAt: Instant? = null,
    val wakeTimeAt: Instant? = null,
    val confidenceScore: Int = 0
)

sealed class SleepEvent {
    object DetectionWindowEntered : SleepEvent()
    object DetectionWindowExited : SleepEvent()
    data class ConfidenceUpdated(val score: Int) : SleepEvent()
    object MotionDetected : SleepEvent()
    object ScreenTurnedOn : SleepEvent()
    object ScreenTurnedOff : SleepEvent()
    object SustainedStillness : SleepEvent()
    object WakeSignalStrong : SleepEvent()
    object FinalizeSession : SleepEvent()
    object Reset : SleepEvent()
}

class SleepStateMachine {

    private val _state = MutableStateFlow(SleepStateContext())
    val state: StateFlow<SleepStateContext> = _state.asStateFlow()

    val currentState: SleepState get() = _state.value.state

    fun transition(event: SleepEvent) {
        val current = _state.value
        val next = when (current.state) {
            SleepState.IDLE -> handleIdle(current, event)
            SleepState.WATCHING -> handleWatching(current, event)
            SleepState.PRE_SLEEP -> handlePreSleep(current, event)
            SleepState.SLEEPING -> handleSleeping(current, event)
            SleepState.POST_SLEEP -> handlePostSleep(current, event)
            SleepState.FINALIZED -> handleFinalized(current, event)
        }
        if (next != current) {
            Timber.d("SleepStateMachine: ${current.state} -> ${next.state} via $event")
            _state.value = next
        }
    }

    private fun handleIdle(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.DetectionWindowEntered -> ctx.copy(
                state = SleepState.WATCHING,
                watchingStartedAt = Instant.now()
            )
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    private fun handleWatching(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.ConfidenceUpdated -> {
                val newCtx = ctx.copy(confidenceScore = event.score)
                if (event.score >= PRE_SLEEP_THRESHOLD) {
                    newCtx.copy(
                        state = SleepState.PRE_SLEEP,
                        preSleepStartedAt = Instant.now()
                    )
                } else newCtx
            }
            is SleepEvent.DetectionWindowExited -> ctx.copy(state = SleepState.IDLE)
            is SleepEvent.MotionDetected -> ctx.copy(confidenceScore = maxOf(0, ctx.confidenceScore - 10))
            is SleepEvent.ScreenTurnedOn -> ctx.copy(confidenceScore = maxOf(0, ctx.confidenceScore - 15))
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    private fun handlePreSleep(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.ConfidenceUpdated -> {
                val newCtx = ctx.copy(confidenceScore = event.score)
                when {
                    event.score >= SLEEP_THRESHOLD -> newCtx.copy(
                        state = SleepState.SLEEPING,
                        sleepOnsetAt = Instant.now()
                    )
                    event.score < PRE_SLEEP_THRESHOLD -> newCtx.copy(state = SleepState.WATCHING)
                    else -> newCtx
                }
            }
            is SleepEvent.ScreenTurnedOn -> ctx.copy(
                state = SleepState.WATCHING,
                confidenceScore = maxOf(0, ctx.confidenceScore - 20)
            )
            is SleepEvent.WakeSignalStrong -> ctx.copy(state = SleepState.WATCHING, confidenceScore = 0)
            is SleepEvent.DetectionWindowExited -> ctx.copy(state = SleepState.IDLE)
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    private fun handleSleeping(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.ScreenTurnedOn -> ctx.copy(
                state = SleepState.POST_SLEEP,
                postSleepStartedAt = Instant.now()
            )
            is SleepEvent.WakeSignalStrong -> ctx.copy(
                state = SleepState.POST_SLEEP,
                postSleepStartedAt = Instant.now()
            )
            is SleepEvent.ConfidenceUpdated -> ctx.copy(confidenceScore = event.score)
            is SleepEvent.FinalizeSession -> ctx.copy(
                state = SleepState.FINALIZED,
                wakeTimeAt = Instant.now()
            )
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    private fun handlePostSleep(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.FinalizeSession -> ctx.copy(
                state = SleepState.FINALIZED,
                wakeTimeAt = ctx.postSleepStartedAt ?: Instant.now()
            )
            is SleepEvent.SustainedStillness -> ctx.copy(
                state = SleepState.SLEEPING,
                postSleepStartedAt = null
            )
            is SleepEvent.WakeSignalStrong -> ctx.copy(
                state = SleepState.FINALIZED,
                wakeTimeAt = Instant.now()
            )
            is SleepEvent.DetectionWindowExited -> ctx.copy(
                state = SleepState.FINALIZED,
                wakeTimeAt = Instant.now()
            )
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    private fun handleFinalized(ctx: SleepStateContext, event: SleepEvent): SleepStateContext {
        return when (event) {
            is SleepEvent.Reset -> SleepStateContext()
            else -> ctx
        }
    }

    fun reset() {
        _state.value = SleepStateContext()
    }

    companion object {
        const val PRE_SLEEP_THRESHOLD = 40
        const val SLEEP_THRESHOLD = 65
    }
}
