package com.mahesh.sparrow.domain.model

/**
 * Sparrow's movement state machine, as described in the product spec:
 * Entering -> Idle -> (Flying <-> Landing) -> Idle, with Sleeping, Paused,
 * and BeingDragged reachable as interruptions from most states.
 *
 * Kept free of any Android dependency so the transition rules can be unit
 * tested directly.
 */
enum class SparrowAnimationState {
    ENTERING,
    IDLE,
    FLYING,
    LANDING,
    SLEEPING,
    BEING_DRAGGED,
    PAUSED;

    /** Whether moving from this state directly to [target] is allowed. */
    fun canTransitionTo(target: SparrowAnimationState): Boolean {
        if (target == this) return false

        // A user grabbing or pausing Sparrow can interrupt almost anything.
        if (target == BEING_DRAGGED && this != ENTERING) return true
        if (target == PAUSED && this != ENTERING) return true

        return target in (VALID_TRANSITIONS[this] ?: emptySet())
    }

    companion object {
        private val VALID_TRANSITIONS: Map<SparrowAnimationState, Set<SparrowAnimationState>> = mapOf(
            ENTERING to setOf(IDLE, LANDING),
            IDLE to setOf(FLYING, SLEEPING, LANDING),
            FLYING to setOf(LANDING),
            LANDING to setOf(IDLE),
            SLEEPING to setOf(IDLE),
            BEING_DRAGGED to setOf(LANDING, IDLE),
            PAUSED to setOf(IDLE)
        )
    }
}
