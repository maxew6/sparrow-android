package com.mahesh.sparrow.domain.model

/**
 * The three pet sizes offered in settings. [dp] is the visible illustration
 * size; [touchTargetDp] is slightly larger so drag/tap gestures stay easy to
 * hit without inflating the actual overlay bounds unnecessarily.
 */
enum class SparrowSize(val dp: Int, val touchTargetDp: Int) {
    SMALL(dp = 48, touchTargetDp = 56),
    MEDIUM(dp = 64, touchTargetDp = 72),
    LARGE(dp = 84, touchTargetDp = 92);

    companion object {
        val Default = SMALL

        fun fromName(name: String?): SparrowSize =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
