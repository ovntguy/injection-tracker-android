package com.deeeelay.injectiontracker.ui.silhouette

import com.deeeelay.injectiontracker.domain.InjectionSite
import kotlin.math.hypot

/**
 * Pure geometry for the code-drawn zone markers (Design polish spec 2026-11-14, items 1–4).
 *
 * Marker centers come only from the locked zones.json cx/cy (mirrored by [InjectionSite]),
 * scaled to the rendered figure. Nothing here moves a zone.
 */
object ZoneGeometry {
    /** Ignore taps farther than this from every zone center. */
    const val REJECT_DISTANCE_DP = 28f

    /** Tap region extends this far past the figure bounds. */
    const val HIT_PADDING_DP = 16f

    /** Never render the 140×220 viewBox smaller than this. */
    const val MIN_DP_PER_UNIT = 1.6f

    /** Figure width is held between these fractions of the screen width. */
    const val MIN_WIDTH_FRACTION = 0.70f
    const val MAX_WIDTH_FRACTION = 0.75f

    // Glyph sizes in dp (Design spec; supersede viewBox dot radii).
    const val REST_RING_DP = 14f
    const val RING_STROKE_DP = 1.5f
    const val LAST_SITE_DOT_DP = 6f
    const val SELECTED_DISC_DP = 20f
    const val SELECTED_HALO_DP = 2f
    const val SELECTED_CHECK_DP = 12f
    const val SELECTED_STROKE_DP = 2f
    const val HOME_DOT_DP = 10f
    const val HOME_HALO_DP = 2f
    const val HOME_RING_STROKE_DP = 1.5f

    /** Home marker overall diameter: dot + halo + ring on both sides. */
    const val HOME_MARKER_DP = HOME_DOT_DP + 2 * HOME_HALO_DP + 2 * HOME_RING_STROKE_DP

    data class Point(val x: Float, val y: Float)

    /** Center of [site] in the rendered figure's local coordinates (any unit, px or dp). */
    fun markerCenter(site: InjectionSite, figureWidth: Float, figureHeight: Float): Point =
        Point(
            x = site.viewportX / InjectionSite.VIEWPORT_WIDTH * figureWidth,
            y = site.viewportY / InjectionSite.VIEWPORT_HEIGHT * figureHeight,
        )

    /**
     * Nearest zone to a tap at ([tapX], [tapY]) in figure-local px, or null when the
     * nearest center is more than [REJECT_DISTANCE_DP] away.
     */
    fun snapToZone(
        tapX: Float,
        tapY: Float,
        figureWidthPx: Float,
        figureHeightPx: Float,
        pxPerDp: Float,
        rejectDp: Float = REJECT_DISTANCE_DP,
    ): InjectionSite? {
        var best: InjectionSite? = null
        var bestPx = Float.MAX_VALUE
        for (site in InjectionSite.entries) {
            val c = markerCenter(site, figureWidthPx, figureHeightPx)
            val d = hypot((c.x - tapX).toDouble(), (c.y - tapY).toDouble()).toFloat()
            if (d < bestPx) {
                bestPx = d
                best = site
            }
        }
        return best.takeIf { bestPx / pxPerDp <= rejectDp }
    }

    /**
     * dp per viewBox unit for the Log figure: fill [availableHeightDp], held to
     * 70–75% of [screenWidthDp], never below [MIN_DP_PER_UNIT] (the column scrolls instead).
     */
    fun logFigureScale(screenWidthDp: Float, availableHeightDp: Float): Float {
        val lo = maxOf(MIN_DP_PER_UNIT, screenWidthDp * MIN_WIDTH_FRACTION / InjectionSite.VIEWPORT_WIDTH)
        val hi = maxOf(lo, screenWidthDp * MAX_WIDTH_FRACTION / InjectionSite.VIEWPORT_WIDTH)
        return (availableHeightDp / InjectionSite.VIEWPORT_HEIGHT).coerceIn(lo, hi)
    }
}

/** Visual state of one zone marker (item 4: differs by shape, fill and label). */
enum class ZoneGlyph { Rest, LastSite, Selected }

fun zoneGlyph(site: InjectionSite, selected: InjectionSite?, lastSite: InjectionSite?): ZoneGlyph =
    when (site) {
        selected -> ZoneGlyph.Selected
        lastSite -> ZoneGlyph.LastSite
        else -> ZoneGlyph.Rest
    }
