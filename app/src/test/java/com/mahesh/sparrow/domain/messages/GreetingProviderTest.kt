package com.mahesh.sparrow.domain.messages

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingProviderTest {

    @Test
    fun `early morning hour selects morning greeting`() {
        assertEquals(GreetingKind.MORNING, GreetingProvider.select(hourOfDay = 5))
    }

    @Test
    fun `late morning hour selects morning greeting`() {
        assertEquals(GreetingKind.MORNING, GreetingProvider.select(hourOfDay = 11))
    }

    @Test
    fun `noon selects hello greeting`() {
        assertEquals(GreetingKind.HELLO, GreetingProvider.select(hourOfDay = 12))
    }

    @Test
    fun `late night selects hello greeting`() {
        assertEquals(GreetingKind.HELLO, GreetingProvider.select(hourOfDay = 23))
    }

    @Test
    fun `just before morning window selects hello greeting`() {
        assertEquals(GreetingKind.HELLO, GreetingProvider.select(hourOfDay = 4))
    }
}
