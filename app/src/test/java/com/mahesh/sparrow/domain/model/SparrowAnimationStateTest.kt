package com.mahesh.sparrow.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SparrowAnimationStateTest {

    @Test
    fun `entering can move to idle or landing`() {
        assertTrue(SparrowAnimationState.ENTERING.canTransitionTo(SparrowAnimationState.IDLE))
        assertTrue(SparrowAnimationState.ENTERING.canTransitionTo(SparrowAnimationState.LANDING))
    }

    @Test
    fun `entering cannot be dragged mid-entrance`() {
        assertFalse(SparrowAnimationState.ENTERING.canTransitionTo(SparrowAnimationState.BEING_DRAGGED))
    }

    @Test
    fun `idle can start flying or sleeping`() {
        assertTrue(SparrowAnimationState.IDLE.canTransitionTo(SparrowAnimationState.FLYING))
        assertTrue(SparrowAnimationState.IDLE.canTransitionTo(SparrowAnimationState.SLEEPING))
    }

    @Test
    fun `idle cannot jump directly to entering`() {
        assertFalse(SparrowAnimationState.IDLE.canTransitionTo(SparrowAnimationState.ENTERING))
    }

    @Test
    fun `flying must land before returning to idle`() {
        assertFalse(SparrowAnimationState.FLYING.canTransitionTo(SparrowAnimationState.IDLE))
        assertTrue(SparrowAnimationState.FLYING.canTransitionTo(SparrowAnimationState.LANDING))
    }

    @Test
    fun `landing resolves to idle`() {
        assertTrue(SparrowAnimationState.LANDING.canTransitionTo(SparrowAnimationState.IDLE))
    }

    @Test
    fun `almost any state can be interrupted by a drag or pause`() {
        assertTrue(SparrowAnimationState.IDLE.canTransitionTo(SparrowAnimationState.BEING_DRAGGED))
        assertTrue(SparrowAnimationState.FLYING.canTransitionTo(SparrowAnimationState.BEING_DRAGGED))
        assertTrue(SparrowAnimationState.SLEEPING.canTransitionTo(SparrowAnimationState.PAUSED))
    }

    @Test
    fun `a state cannot transition to itself`() {
        assertFalse(SparrowAnimationState.IDLE.canTransitionTo(SparrowAnimationState.IDLE))
    }

    @Test
    fun `paused resumes only to idle`() {
        assertTrue(SparrowAnimationState.PAUSED.canTransitionTo(SparrowAnimationState.IDLE))
        assertFalse(SparrowAnimationState.PAUSED.canTransitionTo(SparrowAnimationState.FLYING))
    }
}
