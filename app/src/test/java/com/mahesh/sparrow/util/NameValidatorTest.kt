package com.mahesh.sparrow.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NameValidatorTest {

    @Test
    fun `blank input is rejected`() {
        assertEquals(NameValidationResult.Blank, NameValidator.validate(""))
        assertEquals(NameValidationResult.Blank, NameValidator.validate("   "))
    }

    @Test
    fun `valid name is trimmed`() {
        val result = NameValidator.validate("  Asha  ")
        assertTrue(result is NameValidationResult.Valid)
        assertEquals("Asha", (result as NameValidationResult.Valid).trimmedName)
    }

    @Test
    fun `name at the maximum length is accepted`() {
        val name = "A".repeat(NameValidator.MAX_LENGTH)
        val result = NameValidator.validate(name)
        assertTrue(result is NameValidationResult.Valid)
    }

    @Test
    fun `name over the maximum length is rejected`() {
        val name = "A".repeat(NameValidator.MAX_LENGTH + 1)
        val result = NameValidator.validate(name)
        assertTrue(result is NameValidationResult.TooLong)
        assertEquals(NameValidator.MAX_LENGTH, (result as NameValidationResult.TooLong).maxLength)
    }
}
