package com.solace.sleep

import com.solace.sleep.util.isWithinDetectionWindow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalTime

class TimeExtensionsTest {

    @Test
    fun `detection window spanning midnight includes times at night`() {
        val start = LocalTime.of(21, 0)
        val end = LocalTime.of(10, 0)
        assertTrue(isWithinDetectionWindow(LocalTime.of(22, 0), start, end))
        assertTrue(isWithinDetectionWindow(LocalTime.of(0, 30), start, end))
        assertTrue(isWithinDetectionWindow(LocalTime.of(9, 0), start, end))
    }

    @Test
    fun `detection window spanning midnight excludes midday`() {
        val start = LocalTime.of(21, 0)
        val end = LocalTime.of(10, 0)
        assertFalse(isWithinDetectionWindow(LocalTime.of(12, 0), start, end))
        assertFalse(isWithinDetectionWindow(LocalTime.of(15, 30), start, end))
        assertFalse(isWithinDetectionWindow(LocalTime.of(20, 59), start, end))
    }

    @Test
    fun `detection window same-day works correctly`() {
        val start = LocalTime.of(9, 0)
        val end = LocalTime.of(17, 0)
        assertTrue(isWithinDetectionWindow(LocalTime.of(12, 0), start, end))
        assertFalse(isWithinDetectionWindow(LocalTime.of(20, 0), start, end))
    }
}
