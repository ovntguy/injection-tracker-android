package com.deeeelay.injectiontracker.domain

/**
 * Frontal injection zones in the locked 140×220 silhouette viewport.
 *
 * Facing the figure: screen-left (low X) is anatomical [RIGHT_*];
 * screen-right (high X) is anatomical [LEFT_*]. Centers are the locked
 * v2-tighter-bands geometry (cx/cy unchanged); v1.1 only renames L↔R.
 * Hit testing is nearest-center with a ≥48dp target ([HIT_TARGET_DP]).
 */
enum class InjectionSite(
    val viewportX: Float,
    val viewportY: Float,
) {
    RIGHT_UPPER_ARM_PROXIMAL(47.0f, 60.0f),
    RIGHT_UPPER_ARM_MID(45.5f, 73.0f),
    RIGHT_UPPER_ARM_DISTAL(44.5f, 86.0f),
    LEFT_UPPER_ARM_PROXIMAL(93.0f, 60.0f),
    LEFT_UPPER_ARM_MID(94.5f, 73.0f),
    LEFT_UPPER_ARM_DISTAL(95.5f, 86.0f),
    RIGHT_ABDOMEN_PROXIMAL(60.0f, 92.0f),
    RIGHT_ABDOMEN_MID(59.5f, 105.0f),
    RIGHT_ABDOMEN_DISTAL(59.5f, 118.0f),
    LEFT_ABDOMEN_PROXIMAL(80.0f, 92.0f),
    LEFT_ABDOMEN_MID(80.5f, 105.0f),
    LEFT_ABDOMEN_DISTAL(80.5f, 118.0f),
    RIGHT_UPPER_THIGH_PROXIMAL(61.0f, 134.0f),
    RIGHT_UPPER_THIGH_MID(61.0f, 146.0f),
    RIGHT_UPPER_THIGH_DISTAL(60.5f, 158.0f),
    LEFT_UPPER_THIGH_PROXIMAL(79.0f, 134.0f),
    LEFT_UPPER_THIGH_MID(79.0f, 146.0f),
    LEFT_UPPER_THIGH_DISTAL(79.5f, 158.0f),
    ;

    val zoneId: String get() = name.lowercase()

    companion object {
        const val VIEWPORT_WIDTH = 140f
        const val VIEWPORT_HEIGHT = 220f
        const val ZONE_COUNT = 18
        const val HIT_TARGET_DP = 48f
        const val DISPLAY_DOT_RADIUS = 1.75f
        const val SELECT_CENTER_DOT_RADIUS = 1.5f
        const val HIT_REGIONS_LOCKED = true
        const val CENTERS_VERSION = "v2-tighter-bands"
        const val ZONES_VERSION = "v1.1-lr-swap"

        fun fromPersistedName(raw: String?): InjectionSite? {
            if (raw.isNullOrBlank()) return null
            entries.find { it.name == raw }?.let { return it }
            entries.find { it.zoneId == raw }?.let { return it }
            return when (raw) {
                "LEFT_UPPER_ARM" -> LEFT_UPPER_ARM_MID
                "RIGHT_UPPER_ARM" -> RIGHT_UPPER_ARM_MID
                "LEFT_STOMACH" -> LEFT_ABDOMEN_MID
                "RIGHT_STOMACH" -> RIGHT_ABDOMEN_MID
                "LEFT_UPPER_THIGH" -> LEFT_UPPER_THIGH_MID
                "RIGHT_UPPER_THIGH" -> RIGHT_UPPER_THIGH_MID
                else -> null
            }
        }

        fun nearest(
            viewportX: Float,
            viewportY: Float,
            maxDistance: Float,
        ): InjectionSite? {
            var best: InjectionSite? = null
            var bestDist = Float.MAX_VALUE
            for (site in entries) {
                val dx = site.viewportX - viewportX
                val dy = site.viewportY - viewportY
                val dist = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist < bestDist) {
                    bestDist = dist
                    best = site
                }
            }
            return best.takeIf { bestDist <= maxDistance }
        }
    }
}
