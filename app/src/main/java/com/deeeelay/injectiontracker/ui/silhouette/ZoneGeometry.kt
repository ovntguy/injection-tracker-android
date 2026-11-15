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

    /**
     * Silhouette body extents in viewBox units. Design polish spec (revised 09:45 CST, 2026-11-14):
     * "scale by the BODY, not the 140x220 frame", using zones.json `bodyBounds`
     * (x 32.9–107.1, y 5–215). Main's vendored zones.json predates `bodyBounds`, so the numbers
     * live here; they must match Design's zones.json once it is vendored.
     */
    const val BODY_X = 32.9f
    const val BODY_Y = 5f
    const val BODY_WIDTH = 74.2f
    const val BODY_HEIGHT = 210f

    /** Padding around the body when cropping the Log figure (viewBox units). */
    const val BODY_PADDING = 6f

    /** Log figure dp per viewBox unit: target, and the floor before the screen scrolls. */
    const val TARGET_DP_PER_UNIT = 2.0f
    const val MIN_DP_PER_UNIT = 1.7f

    // Glyph sizes in dp (Design spec; supersede viewBox dot radii).
    const val REST_RING_DP = 14f
    const val RING_STROKE_DP = 1.5f
    const val LAST_SITE_DOT_DP = 6f
    const val SELECTED_DISC_DP = 20f
    const val SELECTED_HALO_DP = 2f
    const val SELECTED_CHECK_DP = 12f
    const val SELECTED_STROKE_DP = 2f
    const val HOME_DOT_DP = 10f
    const val HOME_HALO_DP = 1.5f
    const val HOME_RING_STROKE_DP = 1.5f

    /** Home marker overall diameter: dot + halo + ring on both sides. */
    const val HOME_MARKER_DP = HOME_DOT_DP + 2 * HOME_HALO_DP + 2 * HOME_RING_STROKE_DP

    data class Point(val x: Float, val y: Float)

    /** The part of the 140×220 viewBox that is drawn, in viewBox units. */
    data class ViewWindow(val x: Float, val y: Float, val width: Float, val height: Float)

    /** Whole frame (Home). */
    val FULL_FRAME = ViewWindow(0f, 0f, InjectionSite.VIEWPORT_WIDTH, InjectionSite.VIEWPORT_HEIGHT)

    /** Body bounds + [BODY_PADDING] on every side (Log). */
    val LOG_BODY_CROP = ViewWindow(
        x = BODY_X - BODY_PADDING,
        y = BODY_Y - BODY_PADDING,
        width = BODY_WIDTH + 2 * BODY_PADDING,
        height = BODY_HEIGHT + 2 * BODY_PADDING,
    )

    /**
     * Center of [site] in the rendered figure's local coordinates (any unit, px or dp), where the
     * figure shows [window] of the viewBox scaled to [figureWidth] × [figureHeight].
     */
    fun markerCenter(
        site: InjectionSite,
        figureWidth: Float,
        figureHeight: Float,
        window: ViewWindow = FULL_FRAME,
    ): Point =
        Point(
            x = (site.viewportX - window.x) / window.width * figureWidth,
            y = (site.viewportY - window.y) / window.height * figureHeight,
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
        window: ViewWindow = FULL_FRAME,
        rejectDp: Float = REJECT_DISTANCE_DP,
    ): InjectionSite? {
        var best: InjectionSite? = null
        var bestPx = Float.MAX_VALUE
        for (site in InjectionSite.entries) {
            val c = markerCenter(site, figureWidthPx, figureHeightPx, window)
            val d = hypot((c.x - tapX).toDouble(), (c.y - tapY).toDouble()).toFloat()
            if (d < bestPx) {
                bestPx = d
                best = site
            }
        }
        return best.takeIf { bestPx / pxPerDp <= rejectDp }
    }

    /**
     * dp per viewBox unit for the Log figure: fill [availableHeightDp] with the body crop, up to
     * [TARGET_DP_PER_UNIT], never below [MIN_DP_PER_UNIT] (the screen scrolls instead). No width cap.
     */
    fun logFigureScale(availableHeightDp: Float): Float =
        (availableHeightDp / LOG_BODY_CROP.height).coerceIn(MIN_DP_PER_UNIT, TARGET_DP_PER_UNIT)
}

/** Visual state of one zone marker (item 4: differs by shape, fill and label). */
enum class ZoneGlyph { Rest, LastSite, Selected }

fun zoneGlyph(site: InjectionSite, selected: InjectionSite?, lastSite: InjectionSite?): ZoneGlyph =
    when (site) {
        selected -> ZoneGlyph.Selected
        lastSite -> ZoneGlyph.LastSite
        else -> ZoneGlyph.Rest
    }
