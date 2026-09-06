package com.deeeelay.injectiontracker.domain

/**
 * Frontal injection zones in the locked 140×220 silhouette viewport.
 * X/Y are display coordinates (viewer's left = smaller X).
 * Centers match app/src/main/assets/silhouette_zones.json.
 */
enum class InjectionSite(
    val viewportX: Float,
    val viewportY: Float,
) {
    LEFT_UPPER_ARM_PROXIMAL(46.9f, 68.0f),
    LEFT_UPPER_ARM_MID(45.1f, 84.8f),
    LEFT_UPPER_ARM_DISTAL(43.5f, 101.6f),
    RIGHT_UPPER_ARM_PROXIMAL(93.2f, 68.0f),
    RIGHT_UPPER_ARM_MID(94.9f, 84.8f),
    RIGHT_UPPER_ARM_DISTAL(96.8f, 101.6f),
    LEFT_ABDOMEN_PROXIMAL(60.1f, 93.2f),
    LEFT_ABDOMEN_MID(59.6f, 110.0f),
    LEFT_ABDOMEN_DISTAL(59.7f, 126.8f),
    RIGHT_ABDOMEN_PROXIMAL(79.9f, 93.2f),
    RIGHT_ABDOMEN_MID(80.4f, 110.0f),
    RIGHT_ABDOMEN_DISTAL(80.3f, 126.8f),
    LEFT_UPPER_THIGH_PROXIMAL(60.7f, 139.4f),
    LEFT_UPPER_THIGH_MID(61.4f, 156.2f),
    LEFT_UPPER_THIGH_DISTAL(61.3f, 173.0f),
    RIGHT_UPPER_THIGH_PROXIMAL(79.4f, 139.4f),
    RIGHT_UPPER_THIGH_MID(78.7f, 156.2f),
    RIGHT_UPPER_THIGH_DISTAL(78.9f, 173.0f),
    ;

    val zoneId: String get() = name.lowercase()

    companion object {
        const val VIEWPORT_WIDTH = 140f
        const val VIEWPORT_HEIGHT = 220f
        const val ZONE_COUNT = 18
        const val HIT_TARGET_DP = 48f
        const val DISPLAY_DOT_RADIUS = 1.75f
        const val SELECT_CENTER_DOT_RADIUS = 1.5f

        fun fromPersistedName(raw: String?): InjectionSite? {
            if (raw.isNullOrBlank()) return null
            entries.find { it.name == raw }?.let { return it }
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
