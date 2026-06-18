package com.solace.sleep

import com.solace.sleep.detection.SleepEvent
import com.solace.sleep.detection.SleepState
import com.solace.sleep.detection.SleepStateMachine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SleepStateMachineTest {

    private lateinit var stateMachine: SleepStateMachine

    @BeforeEach
    fun setUp() {
        stateMachine = SleepStateMachine()
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(SleepState.IDLE, stateMachine.currentState)
    }

    @Test
    fun `enters WATCHING when detection window entered`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        assertEquals(SleepState.WATCHING, stateMachine.currentState)
    }

    @Test
    fun `transitions to PRE_SLEEP at threshold 40`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        stateMachine.transition(SleepEvent.ConfidenceUpdated(40))
        assertEquals(SleepState.PRE_SLEEP, stateMachine.currentState)
    }

    @Test
    fun `transitions to SLEEPING at threshold 65`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        stateMachine.transition(SleepEvent.ConfidenceUpdated(40))
        stateMachine.transition(SleepEvent.ConfidenceUpdated(65))
        assertEquals(SleepState.SLEEPING, stateMachine.currentState)
    }

    @Test
    fun `screen on from SLEEPING goes to POST_SLEEP`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        stateMachine.transition(SleepEvent.ConfidenceUpdated(40))
        stateMachine.transition(SleepEvent.ConfidenceUpdated(65))
        stateMachine.transition(SleepEvent.ScreenTurnedOn)
        assertEquals(SleepState.POST_SLEEP, stateMachine.currentState)
    }

    @Test
    fun `reset from any state returns to IDLE`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        stateMachine.transition(SleepEvent.ConfidenceUpdated(65))
        stateMachine.transition(SleepEvent.Reset)
        assertEquals(SleepState.IDLE, stateMachine.currentState)
    }

    @Test
    fun `detection window exit from WATCHING returns to IDLE`() {
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
        stateMachine.transition(SleepEvent.DetectionWindowExited)
        assertEquals(SleepState.IDLE, stateMachine.currentState)
    }
}
