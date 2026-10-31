package com.hb.surfingcompose

import com.hb.surfingcompose.presentation.screens.CookingTimer
import org.junit.Assert.*
import org.junit.Test

class CookingTimerTest {
    @Test fun deadlineCatchesUpAfterLongBackgroundGap() {
        val timer = CookingTimer().start(60_000, 10_000)
        assertEquals(45_000L, timer.tick(25_000).remainingMs)
        val finished = timer.tick(90_000)
        assertTrue(finished.finished)
        assertNull(finished.deadlineMs)
        assertEquals(0L, finished.remainingMs)
    }

    @Test fun pauseAndResumePreserveRemainingTime() {
        val timer = CookingTimer().start(60_000, 10_000).pause(25_000)
        assertEquals(45_000L, timer.tick(100_000).remainingMs)
        assertFalse(timer.finished)
        assertEquals(40_000L, timer.resume(100_000).tick(105_000).remainingMs)
    }

    @Test fun pausingAnExpiredTimerCannotRestartIt() {
        val timer = CookingTimer().start(1000, 0).pause(2000).resume(3000)
        assertTrue(timer.finished)
        assertNull(timer.deadlineMs)
        assertFalse(CookingTimer().finished)
    }
}
