package com.deeeelay.injectiontracker.domain

/**
 * Display label for a locked zone id, e.g. "Right thigh, mid".
 *
 * Format is "{Right|Left} {arm|abdomen|thigh}, {upper|mid|lower}" with
 * proximal → upper and distal → lower. Zone ids are never renamed; this only maps them.
 */
object ZoneReadout {
    fun label(site: InjectionSite): String {
        val parts = site.zoneId.split('_')
        val side = when (parts.first()) {
            "right" -> "Right"
            "left" -> "Left"
            else -> error("Unknown side in ${site.zoneId}")
        }
        val band = when (parts.last()) {
            "proximal" -> "upper"
            "mid" -> "mid"
            "distal" -> "lower"
            else -> error("Unknown band in ${site.zoneId}")
        }
        val region = when (parts.subList(1, parts.size - 1).joinToString("_")) {
            "upper_arm" -> "arm"
            "abdomen" -> "abdomen"
            "upper_thigh" -> "thigh"
            else -> error("Unknown region in ${site.zoneId}")
        }
        return "$side $region, $band"
    }
}
