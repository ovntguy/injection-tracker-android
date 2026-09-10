package com.deeeelay.injectiontracker.ui.silhouette

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.InjectionSite

enum class SilhouetteMode { Display, Select }

private const val VIEWPORT_W = InjectionSite.VIEWPORT_WIDTH
private const val VIEWPORT_H = InjectionSite.VIEWPORT_HEIGHT
private val MinHitTarget = InjectionSite.HIT_TARGET_DP.dp

/** Locked VectorDrawable resource for Home display / Log Done select. Period dots are in the VDs. */
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

@Composable
fun InjectionSilhouette(
    modifier: Modifier = Modifier,
    mode: SilhouetteMode,
    markedSite: InjectionSite? = null,
    onSiteSelected: ((InjectionSite) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val drawableRes = silhouetteDrawableRes(mode, markedSite)
    val imageDescription = when {
        markedSite != null -> siteLabel(markedSite)
        mode == SilhouetteMode.Display -> stringResource(R.string.home_last_site)
        else -> stringResource(R.string.log_done_tap_site)
    }

    BoxWithConstraints(modifier = modifier) {
        val scale = minOf(
            constraints.maxWidth / VIEWPORT_W,
            constraints.maxHeight / VIEWPORT_H,
        ).coerceAtLeast(1f)
        val widthPx = VIEWPORT_W * scale
        val heightPx = VIEWPORT_H * scale
        val widthDp = with(density) { widthPx.toDp() }
        val heightDp = with(density) { heightPx.toDp() }
        val maxDistanceViewport = InjectionSite.HIT_TARGET_DP * VIEWPORT_W / widthDp.value

        Box(Modifier.size(widthDp, heightDp)) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = imageDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            if (mode == SilhouetteMode.Select && onSiteSelected != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(widthPx, heightPx, maxDistanceViewport) {
                            detectTapGestures { offset ->
                                val vx = offset.x / widthPx * VIEWPORT_W
                                val vy = offset.y / heightPx * VIEWPORT_H
                                InjectionSite.nearest(vx, vy, maxDistanceViewport)
                                    ?.let(onSiteSelected)
                            }
                        },
                )
                InjectionSite.entries.forEach { site ->
                    val cx = widthDp * (site.viewportX / VIEWPORT_W)
                    val cy = heightDp * (site.viewportY / VIEWPORT_H)
                    val label = siteLabel(site)
                    Box(
                        modifier = Modifier
                            .offset(x = cx - MinHitTarget / 2, y = cy - MinHitTarget / 2)
                            .size(MinHitTarget)
                            .semantics {
                                contentDescription = label
                                onClick {
                                    onSiteSelected(site)
                                    true
                                }
                            },
                    )
                }
            }
        }
    }
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
