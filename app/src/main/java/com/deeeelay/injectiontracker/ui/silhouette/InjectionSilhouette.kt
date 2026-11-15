package com.deeeelay.injectiontracker.ui.silhouette

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.ZoneReadout

enum class SilhouetteMode { Display, Select }

private const val VIEWPORT_W = InjectionSite.VIEWPORT_WIDTH
private const val VIEWPORT_H = InjectionSite.VIEWPORT_HEIGHT
private val MinHitTarget = InjectionSite.HIT_TARGET_DP.dp

/**
 * Base art without zone dots; markers are drawn in code from zones.json cx/cy.
 * Both Home and Log use Visual's `_nomarkers` art and draw R/L in code ([SideLabels]) in one
 * style (Design Critique round 2: the baked Log glyphs were ~24 dp gray, heavier than the heading).
 */
@DrawableRes
fun silhouetteBaseDrawableRes(mode: SilhouetteMode): Int = when (mode) {
    SilhouetteMode.Display -> R.drawable.silhouette_display_empty_nomarkers
    SilhouetteMode.Select -> R.drawable.silhouette_select_unselected_nomarkers
}

/** Code-drawn R/L for Home and Log, placed where Visual's baked glyphs sit in the base art. */
object SideLabels {
    /** Glyph centers in viewBox units, from the marker_R / marker_L paths in the base art. */
    const val RIGHT_X = 22f
    const val LEFT_X = 118f
    const val Y = 24f

    /** Text size, bold, onSurfaceVariant on both screens (Design Critique round 2: ~14sp). */
    const val TEXT_SP = 14f

    /** Below this figure width the labels would crowd the head and arms, so they are left off. */
    const val MIN_FIGURE_WIDTH_DP = 100f

    fun fits(figureWidthDp: Float): Boolean = figureWidthDp >= MIN_FIGURE_WIDTH_DP
}

/**
 * Legacy per-zone VectorDrawable mapping (dots baked in). Not used for rendering since the
 * 2026-11-14 Design Critique review (item A): markers are drawn in code over the base drawables
 * from [silhouetteBaseDrawableRes]. Kept until Visual/Design retire the per-zone files.
 */
@DrawableRes
fun silhouetteDrawableRes(mode: SilhouetteMode, site: InjectionSite?): Int {
    return when (mode) {
        SilhouetteMode.Display -> when (site) {
            null -> R.drawable.silhouette_display_empty
            InjectionSite.LEFT_UPPER_ARM_PROXIMAL -> R.drawable.silhouette_display_left_upper_arm_proximal
            InjectionSite.LEFT_UPPER_ARM_MID -> R.drawable.silhouette_display_left_upper_arm_mid
            InjectionSite.LEFT_UPPER_ARM_DISTAL -> R.drawable.silhouette_display_left_upper_arm_distal
            InjectionSite.RIGHT_UPPER_ARM_PROXIMAL -> R.drawable.silhouette_display_right_upper_arm_proximal
            InjectionSite.RIGHT_UPPER_ARM_MID -> R.drawable.silhouette_display_right_upper_arm_mid
            InjectionSite.RIGHT_UPPER_ARM_DISTAL -> R.drawable.silhouette_display_right_upper_arm_distal
            InjectionSite.LEFT_ABDOMEN_PROXIMAL -> R.drawable.silhouette_display_left_abdomen_proximal
            InjectionSite.LEFT_ABDOMEN_MID -> R.drawable.silhouette_display_left_abdomen_mid
            InjectionSite.LEFT_ABDOMEN_DISTAL -> R.drawable.silhouette_display_left_abdomen_distal
            InjectionSite.RIGHT_ABDOMEN_PROXIMAL -> R.drawable.silhouette_display_right_abdomen_proximal
            InjectionSite.RIGHT_ABDOMEN_MID -> R.drawable.silhouette_display_right_abdomen_mid
            InjectionSite.RIGHT_ABDOMEN_DISTAL -> R.drawable.silhouette_display_right_abdomen_distal
            InjectionSite.LEFT_UPPER_THIGH_PROXIMAL -> R.drawable.silhouette_display_left_upper_thigh_proximal
            InjectionSite.LEFT_UPPER_THIGH_MID -> R.drawable.silhouette_display_left_upper_thigh_mid
            InjectionSite.LEFT_UPPER_THIGH_DISTAL -> R.drawable.silhouette_display_left_upper_thigh_distal
            InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL -> R.drawable.silhouette_display_right_upper_thigh_proximal
            InjectionSite.RIGHT_UPPER_THIGH_MID -> R.drawable.silhouette_display_right_upper_thigh_mid
            InjectionSite.RIGHT_UPPER_THIGH_DISTAL -> R.drawable.silhouette_display_right_upper_thigh_distal
        }
        SilhouetteMode.Select -> when (site) {
            null -> R.drawable.silhouette_select_unselected
            InjectionSite.LEFT_UPPER_ARM_PROXIMAL -> R.drawable.silhouette_select_left_upper_arm_proximal
            InjectionSite.LEFT_UPPER_ARM_MID -> R.drawable.silhouette_select_left_upper_arm_mid
            InjectionSite.LEFT_UPPER_ARM_DISTAL -> R.drawable.silhouette_select_left_upper_arm_distal
            InjectionSite.RIGHT_UPPER_ARM_PROXIMAL -> R.drawable.silhouette_select_right_upper_arm_proximal
            InjectionSite.RIGHT_UPPER_ARM_MID -> R.drawable.silhouette_select_right_upper_arm_mid
            InjectionSite.RIGHT_UPPER_ARM_DISTAL -> R.drawable.silhouette_select_right_upper_arm_distal
            InjectionSite.LEFT_ABDOMEN_PROXIMAL -> R.drawable.silhouette_select_left_abdomen_proximal
            InjectionSite.LEFT_ABDOMEN_MID -> R.drawable.silhouette_select_left_abdomen_mid
            InjectionSite.LEFT_ABDOMEN_DISTAL -> R.drawable.silhouette_select_left_abdomen_distal
            InjectionSite.RIGHT_ABDOMEN_PROXIMAL -> R.drawable.silhouette_select_right_abdomen_proximal
            InjectionSite.RIGHT_ABDOMEN_MID -> R.drawable.silhouette_select_right_abdomen_mid
            InjectionSite.RIGHT_ABDOMEN_DISTAL -> R.drawable.silhouette_select_right_abdomen_distal
            InjectionSite.LEFT_UPPER_THIGH_PROXIMAL -> R.drawable.silhouette_select_left_upper_thigh_proximal
            InjectionSite.LEFT_UPPER_THIGH_MID -> R.drawable.silhouette_select_left_upper_thigh_mid
            InjectionSite.LEFT_UPPER_THIGH_DISTAL -> R.drawable.silhouette_select_left_upper_thigh_distal
            InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL -> R.drawable.silhouette_select_right_upper_thigh_proximal
            InjectionSite.RIGHT_UPPER_THIGH_MID -> R.drawable.silhouette_select_right_upper_thigh_mid
            InjectionSite.RIGHT_UPPER_THIGH_DISTAL -> R.drawable.silhouette_select_right_upper_thigh_distal
        }
    }
}

fun silhouetteDrawableName(mode: SilhouetteMode, site: InjectionSite?): String {
    val prefix = if (mode == SilhouetteMode.Display) "silhouette_display" else "silhouette_select"
    return when {
        site == null && mode == SilhouetteMode.Display -> "${prefix}_empty"
        site == null -> "${prefix}_unselected"
        else -> "${prefix}_${site.zoneId}"
    }
}

/**
 * Silhouette with code-drawn zone markers.
 *
 * - [SilhouetteMode.Display] (Home): [markedSite] is the last site, drawn as the 17 dp Home marker.
 * - [SilhouetteMode.Select] (Log): all 18 zones show a 14 dp hollow ring; [lastSite] gets a
 *   center dot; [markedSite] is the selection (20 dp disc + check). Taps within [hitPadding] of
 *   the figure snap to the nearest zone; taps > 28 dp from every center are ignored.
 */
@Composable
fun InjectionSilhouette(
    modifier: Modifier = Modifier,
    mode: SilhouetteMode,
    markedSite: InjectionSite? = null,
    lastSite: InjectionSite? = null,
    hitPadding: Dp = 0.dp,
    window: ZoneGeometry.ViewWindow = ZoneGeometry.FULL_FRAME,
    description: String? = null,
    /** Overall last-site marker diameter on Home; defaults to the 17 dp (spec "16 dp") glyph. */
    homeMarkerDiameterDp: Float = ZoneGeometry.HOME_MARKER_DP,
    /** When true, R/L are omitted even if the figure is wide enough (Home compact height). */
    hideSideLabels: Boolean = false,
    onSiteSelected: ((InjectionSite) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val imageDescription = description ?: when {
        markedSite != null -> siteLabel(markedSite)
        mode == SilhouetteMode.Display -> stringResource(R.string.home_last_site)
        else -> stringResource(R.string.log_done_tap_site)
    }
    val colors = MaterialTheme.colorScheme
    val check = rememberVectorPainter(Icons.Filled.Check)

    BoxWithConstraints(modifier = modifier) {
        val padPx = with(density) { hitPadding.toPx() }
        val scale = minOf(
            (constraints.maxWidth - 2 * padPx) / window.width,
            (constraints.maxHeight - 2 * padPx) / window.height,
        ).coerceAtLeast(1f)
        val widthPx = window.width * scale
        val heightPx = window.height * scale
        val widthDp = with(density) { widthPx.toDp() }
        val heightDp = with(density) { heightPx.toDp() }
        val selectable = mode == SilhouetteMode.Select && onSiteSelected != null

        Box(
            modifier = Modifier
                .size(widthDp + hitPadding * 2, heightDp + hitPadding * 2)
                .then(
                    if (selectable) {
                        Modifier.pointerInput(widthPx, heightPx, padPx) {
                            detectTapGestures { tap ->
                                ZoneGeometry.snapToZone(
                                    tapX = tap.x - padPx,
                                    tapY = tap.y - padPx,
                                    figureWidthPx = widthPx,
                                    figureHeightPx = heightPx,
                                    pxPerDp = density.density,
                                    window = window,
                                )?.let { onSiteSelected?.invoke(it) }
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val art = painterResource(silhouetteBaseDrawableRes(mode))
            Box(
                Modifier
                    .size(widthDp, heightDp)
                    .semantics { contentDescription = imageDescription },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    // Draw the whole 140×220 art, shifted so [window] lands in this box. The body lies
                    // inside every window, so nothing needs clipping.
                    val unit = size.width / window.width
                    translate(-window.x * unit, -window.y * unit) {
                        with(art) { draw(Size(VIEWPORT_W * unit, VIEWPORT_H * unit)) }
                    }
                }
                Canvas(Modifier.fillMaxSize()) {
                    if (mode == SilhouetteMode.Display) {
                        markedSite?.let { site ->
                            val c = ZoneGeometry.markerCenter(site, size.width, size.height, window)
                            drawHomeMarker(Offset(c.x, c.y), colors.primary, colors.surface, homeMarkerDiameterDp)
                        }
                    } else {
                        drawSelectMarkers(
                            window = window,
                            selected = markedSite,
                            lastSite = lastSite,
                            colors = MarkerColors(
                                rest = colors.onSurfaceVariant,
                                lastRing = colors.outline,
                                lastDot = colors.onSurfaceVariant,
                                selected = colors.primary,
                                halo = colors.surface,
                                check = colors.onPrimary,
                            ),
                            check = check,
                        )
                    }
                }
                if (!hideSideLabels && SideLabels.fits(widthDp.value)) {
                    SideLabel(stringResource(R.string.silhouette_side_right), SideLabels.RIGHT_X, window, widthDp, heightDp)
                    SideLabel(stringResource(R.string.silhouette_side_left), SideLabels.LEFT_X, window, widthDp, heightDp)
                }
                if (selectable) {
                    InjectionSite.entries.forEach { site ->
                        val c = ZoneGeometry.markerCenter(site, widthDp.value, heightDp.value, window)
                        val label = ZoneReadout.label(site)
                        val isSelected = site == markedSite
                        Box(
                            modifier = Modifier
                                .offset(x = c.x.dp - MinHitTarget / 2, y = c.y.dp - MinHitTarget / 2)
                                .size(MinHitTarget)
                                .semantics {
                                    contentDescription = label
                                    selected = isSelected
                                    onClick {
                                        onSiteSelected?.invoke(site)
                                        true
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SideLabel(
    text: String,
    viewportX: Float,
    window: ZoneGeometry.ViewWindow,
    widthDp: Dp,
    heightDp: Dp,
) {
    // On Log the glyph centers (x 22 / 118) sit just outside the body crop, so the labels draw
    // beside the figure, in the hit padding; the parent does not clip.
    val box = 24.dp
    Box(
        modifier = Modifier
            .offset(
                x = widthDp * ((viewportX - window.x) / window.width) - box / 2,
                y = heightDp * ((SideLabels.Y - window.y) / window.height) - box / 2,
            )
            .size(box)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = SideLabels.TEXT_SP.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private class MarkerColors(
    val rest: Color,
    val lastRing: Color,
    val lastDot: Color,
    val selected: Color,
    val halo: Color,
    val check: Color,
)

private fun DrawScope.drawSelectMarkers(
    window: ZoneGeometry.ViewWindow,
    selected: InjectionSite?,
    lastSite: InjectionSite?,
    colors: MarkerColors,
    check: VectorPainter,
) {
    val ringStroke = ZoneGeometry.RING_STROKE_DP.dp.toPx()
    val ringRadius = ZoneGeometry.REST_RING_DP.dp.toPx() / 2 - ringStroke / 2
    // Rings and last-site first; the selected glyph draws above its neighbors.
    InjectionSite.entries.forEach { site ->
        val c = ZoneGeometry.markerCenter(site, size.width, size.height, window).let { Offset(it.x, it.y) }
        when (zoneGlyph(site, selected, lastSite)) {
            ZoneGlyph.Rest -> drawCircle(colors.rest, ringRadius, c, style = Stroke(ringStroke))
            ZoneGlyph.LastSite -> {
                drawCircle(colors.lastRing, ringRadius, c, style = Stroke(ringStroke))
                drawCircle(colors.lastDot, ZoneGeometry.LAST_SITE_DOT_DP.dp.toPx() / 2, c)
            }
            ZoneGlyph.Selected -> Unit
        }
    }
    selected?.let { site ->
        val c = ZoneGeometry.markerCenter(site, size.width, size.height, window).let { Offset(it.x, it.y) }
        val discRadius = ZoneGeometry.SELECTED_DISC_DP.dp.toPx() / 2
        drawCircle(colors.halo, discRadius + ZoneGeometry.SELECTED_HALO_DP.dp.toPx(), c)
        drawCircle(colors.selected, discRadius, c)
        val checkPx = ZoneGeometry.SELECTED_CHECK_DP.dp.toPx()
        translate(c.x - checkPx / 2, c.y - checkPx / 2) {
            with(check) { draw(Size(checkPx, checkPx), colorFilter = ColorFilter.tint(colors.check)) }
        }
    }
}

private fun DrawScope.drawHomeMarker(
    c: Offset,
    primary: Color,
    surface: Color,
    diameterDp: Float = ZoneGeometry.HOME_MARKER_DP,
) {
    val scale = diameterDp / ZoneGeometry.HOME_MARKER_DP
    val dotR = ZoneGeometry.HOME_DOT_DP.dp.toPx() / 2 * scale
    val haloR = dotR + ZoneGeometry.HOME_HALO_DP.dp.toPx() * scale
    val ringStroke = ZoneGeometry.HOME_RING_STROKE_DP.dp.toPx() * scale
    drawCircle(surface, haloR, c)
    drawCircle(primary, haloR + ringStroke / 2, c, style = Stroke(ringStroke))
    drawCircle(primary, dotR, c)
}

@Composable
fun siteLabel(site: InjectionSite): String {
    val res = when (site) {
        InjectionSite.LEFT_UPPER_ARM_PROXIMAL -> R.string.site_left_upper_arm_proximal
        InjectionSite.LEFT_UPPER_ARM_MID -> R.string.site_left_upper_arm_mid
        InjectionSite.LEFT_UPPER_ARM_DISTAL -> R.string.site_left_upper_arm_distal
        InjectionSite.RIGHT_UPPER_ARM_PROXIMAL -> R.string.site_right_upper_arm_proximal
        InjectionSite.RIGHT_UPPER_ARM_MID -> R.string.site_right_upper_arm_mid
        InjectionSite.RIGHT_UPPER_ARM_DISTAL -> R.string.site_right_upper_arm_distal
        InjectionSite.LEFT_ABDOMEN_PROXIMAL -> R.string.site_left_abdomen_proximal
        InjectionSite.LEFT_ABDOMEN_MID -> R.string.site_left_abdomen_mid
        InjectionSite.LEFT_ABDOMEN_DISTAL -> R.string.site_left_abdomen_distal
        InjectionSite.RIGHT_ABDOMEN_PROXIMAL -> R.string.site_right_abdomen_proximal
        InjectionSite.RIGHT_ABDOMEN_MID -> R.string.site_right_abdomen_mid
        InjectionSite.RIGHT_ABDOMEN_DISTAL -> R.string.site_right_abdomen_distal
        InjectionSite.LEFT_UPPER_THIGH_PROXIMAL -> R.string.site_left_upper_thigh_proximal
        InjectionSite.LEFT_UPPER_THIGH_MID -> R.string.site_left_upper_thigh_mid
        InjectionSite.LEFT_UPPER_THIGH_DISTAL -> R.string.site_left_upper_thigh_distal
        InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL -> R.string.site_right_upper_thigh_proximal
        InjectionSite.RIGHT_UPPER_THIGH_MID -> R.string.site_right_upper_thigh_mid
        InjectionSite.RIGHT_UPPER_THIGH_DISTAL -> R.string.site_right_upper_thigh_distal
    }
    return stringResource(res)
}
