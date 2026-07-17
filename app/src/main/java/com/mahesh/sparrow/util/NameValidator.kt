package com.mahesh.sparrow.util

sealed class NameValidationResult {
    data class Valid(val trimmedName: String) : NameValidationResult()
    data object Blank : NameValidationResult()
    data class TooLong(val maxLength: Int) : NameValidationResult()
}

/** Validates the name entered in onboarding / settings. No Android dependency. */
object NameValidator {
    const val MAX_LENGTH = 30

    fun validate(rawInput: String): NameValidationResult {
        val trimmed = rawInput.trim()
        return when {
            trimmed.isEmpty() -> NameValidationResult.Blank
            trimmed.length > MAX_LENGTH -> NameValidationResult.TooLong(MAX_LENGTH)
            else -> NameValidationResult.Valid(trimmed)
        }
    }
}
