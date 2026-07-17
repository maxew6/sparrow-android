package com.mahesh.sparrow.domain.messages

/** Which greeting string to show; the caller resolves this to actual text. */
enum class GreetingKind { MORNING, HELLO }

/**
 * Picks a greeting purely from the hour of day, so it can be unit tested
 * without any Android Context or clock dependency.
 */
object GreetingProvider {
    private val MORNING_HOURS = 5..11

    fun select(hourOfDay: Int): GreetingKind =
        if (hourOfDay in MORNING_HOURS) GreetingKind.MORNING else GreetingKind.HELLO
}
