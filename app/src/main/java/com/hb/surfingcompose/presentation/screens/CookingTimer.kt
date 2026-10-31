package com.hb.surfingcompose.presentation.screens

/** A monotonic deadline avoids timer drift and catches up after backgrounding. */
data class CookingTimer(
    val remainingMs: Long = 0,
    val deadlineMs: Long? = null,
    val started: Boolean = false
) {
    val finished get() = started && deadlineMs == null && remainingMs == 0L
    fun start(durationMs: Long, now: Long) = CookingTimer(durationMs.coerceAtLeast(1), now + durationMs.coerceAtLeast(1), true)
    fun tick(now: Long): CookingTimer {
        val deadline = deadlineMs ?: return this
        val remaining = (deadline - now).coerceAtLeast(0)
        return copy(remainingMs = remaining, deadlineMs = deadline.takeIf { remaining > 0 })
    }
    fun pause(now: Long) = tick(now).copy(deadlineMs = null)
    fun resume(now: Long) = if (remainingMs > 0) copy(deadlineMs = now + remainingMs) else this
}
