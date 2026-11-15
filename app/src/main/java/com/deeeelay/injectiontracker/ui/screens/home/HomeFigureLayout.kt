package com.deeeelay.injectiontracker.ui.screens.home

import com.deeeelay.injectiontracker.ui.silhouette.ZoneGeometry

/**
 * Home last-site figure height (Design polish spec, BUILD FROM HERE, rev 2026-11-15).
 *
 * Figure height is the leftover space after the rest of Home at its natural height,
 * clamped to [[MIN_DP], [MAX_DP]]. Home scrolls only when leftover space is under [MIN_DP].
 * When the figure is under [COMPACT_HEIGHT_DP], R/L are hidden and the last-site marker is 12 dp.
 */
object HomeFigureLayout {
    const val MIN_DP = 88f

    /** 1.6 dp per bodyBounds unit (height 210) → 336 dp. */
    const val DP_PER_BODY_UNIT = 1.6f
    const val MAX_DP = DP_PER_BODY_UNIT * ZoneGeometry.BODY_HEIGHT

    const val COMPACT_HEIGHT_DP = 160f
    const val COMPACT_MARKER_DP = 12f

    fun figureHeight(leftoverDp: Float): Float = leftoverDp.coerceIn(MIN_DP, MAX_DP)

    fun scrolls(leftoverDp: Float): Boolean = leftoverDp < MIN_DP

    fun compact(figureHeightDp: Float): Boolean = figureHeightDp < COMPACT_HEIGHT_DP

    fun markerDiameterDp(figureHeightDp: Float): Float =
        if (compact(figureHeightDp)) COMPACT_MARKER_DP else ZoneGeometry.HOME_MARKER_DP
}
