package com.deeeelay.injectiontracker.domain

/**
 * Frontal injection zones in the locked 140×220 silhouette viewport.
 * X/Y are display coordinates (viewer's left = smaller X).
 */
enum class InjectionSite(
    val viewportX: Float,
    val viewportY: Float,
) {
    LEFT_UPPER_ARM(46.1f, 80.6f),
    RIGHT_UPPER_ARM(93.9f, 80.6f),
    LEFT_STOMACH(61.1f, 105.8f),
    RIGHT_STOMACH(78.9f, 105.8f),
    LEFT_UPPER_THIGH(60.2f, 147.8f),
    RIGHT_UPPER_THIGH(79.8f, 147.8f),
    ;

    companion object {
        const val VIEWPORT_WIDTH = 140f
        const val VIEWPORT_HEIGHT = 220f

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
