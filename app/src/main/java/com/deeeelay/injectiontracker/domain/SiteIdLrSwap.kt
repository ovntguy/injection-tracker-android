package com.deeeelay.injectiontracker.domain

/**
 * One-shot v1.1 Left↔Right rename for persisted site ids.
 * Same body spot, corrected anatomical name. Involutive: swap(swap(x)) == x.
 */
object SiteIdLrSwap {
    fun swapPersisted(raw: String?): String? {
        if (raw.isNullOrBlank()) return raw
        return when {
            raw.startsWith("LEFT_") -> "RIGHT_" + raw.removePrefix("LEFT_")
            raw.startsWith("RIGHT_") -> "LEFT_" + raw.removePrefix("RIGHT_")
            raw.startsWith("left_") -> "right_" + raw.removePrefix("left_")
            raw.startsWith("right_") -> "left_" + raw.removePrefix("right_")
            else -> raw
        }
    }

    fun looksLikeSiteId(raw: String): Boolean {
        if (InjectionSite.fromPersistedName(raw) != null) return true
        return raw.startsWith("LEFT_") ||
            raw.startsWith("RIGHT_") ||
            raw.startsWith("left_") ||
            raw.startsWith("right_")
    }
}
