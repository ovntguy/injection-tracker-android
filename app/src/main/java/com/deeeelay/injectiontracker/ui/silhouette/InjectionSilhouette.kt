package com.deeeelay.injectiontracker.ui.silhouette

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.InjectionSite

enum class SilhouetteMode { Display, Select }

private const val VIEWPORT_W = InjectionSite.VIEWPORT_WIDTH
private const val VIEWPORT_H = InjectionSite.VIEWPORT_HEIGHT
private val MinHitTarget = 48.dp

@DrawableRes
fun InjectionSite.displayDrawableRes(): Int = when (this) {
    InjectionSite.LEFT_UPPER_ARM -> R.drawable.silhouette_display_left_upper_arm
    InjectionSite.RIGHT_UPPER_ARM -> R.drawable.silhouette_display_right_upper_arm
    InjectionSite.LEFT_STOMACH -> R.drawable.silhouette_display_left_stomach
    InjectionSite.RIGHT_STOMACH -> R.drawable.silhouette_display_right_stomach
    InjectionSite.LEFT_UPPER_THIGH -> R.drawable.silhouette_display_left_upper_thigh
    InjectionSite.RIGHT_UPPER_THIGH -> R.drawable.silhouette_display_right_upper_thigh
}

@DrawableRes
fun InjectionSite.selectDrawableRes(): Int = when (this) {
    InjectionSite.LEFT_UPPER_ARM -> R.drawable.silhouette_select_left_upper_arm
    InjectionSite.RIGHT_UPPER_ARM -> R.drawable.silhouette_select_right_upper_arm
    InjectionSite.LEFT_STOMACH -> R.drawable.silhouette_select_left_stomach
    InjectionSite.RIGHT_STOMACH -> R.drawable.silhouette_select_right_stomach
    InjectionSite.LEFT_UPPER_THIGH -> R.drawable.silhouette_select_left_upper_thigh
    InjectionSite.RIGHT_UPPER_THIGH -> R.drawable.silhouette_select_right_upper_thigh
}

@Composable
fun InjectionSilhouette(
    modifier: Modifier = Modifier,
    mode: SilhouetteMode,
    markedSite: InjectionSite? = null,
    onSiteSelected: ((InjectionSite) -> Unit)? = null,
) {
    val drawableRes = when (mode) {
        SilhouetteMode.Display ->
            markedSite?.displayDrawableRes() ?: R.drawable.silhouette_display_empty
        SilhouetteMode.Select ->
            markedSite?.selectDrawableRes() ?: R.drawable.silhouette_select_unselected
    }
    val density = LocalDensity.current
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

        Box(Modifier.size(widthDp, heightDp)) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = imageDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            if (mode == SilhouetteMode.Select && onSiteSelected != null) {
                InjectionSite.entries.forEach { site ->
                    val cx = widthDp * (site.viewportX / VIEWPORT_W)
                    val cy = heightDp * (site.viewportY / VIEWPORT_H)
                    val label = siteLabel(site)
                    Box(
                        modifier = Modifier
                            .offset(x = cx - MinHitTarget / 2, y = cy - MinHitTarget / 2)
                            .size(MinHitTarget)
                            .clip(CircleShape)
                            .semantics { contentDescription = label }
                            .clickable { onSiteSelected(site) },
                    )
                }
            }
        }
    }
}

@Composable
fun siteLabel(site: InjectionSite): String {
    val res = when (site) {
        InjectionSite.LEFT_UPPER_ARM -> R.string.site_left_upper_arm
        InjectionSite.RIGHT_UPPER_ARM -> R.string.site_right_upper_arm
        InjectionSite.LEFT_STOMACH -> R.string.site_left_stomach
        InjectionSite.RIGHT_STOMACH -> R.string.site_right_stomach
        InjectionSite.LEFT_UPPER_THIGH -> R.string.site_left_upper_thigh
        InjectionSite.RIGHT_UPPER_THIGH -> R.string.site_right_upper_thigh
    }
    return stringResource(res)
}
