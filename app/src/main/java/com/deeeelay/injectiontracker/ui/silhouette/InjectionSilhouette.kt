package com.deeeelay.injectiontracker.ui.silhouette

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.theme.BodyFill
import com.deeeelay.injectiontracker.ui.theme.DisplayStroke
import com.deeeelay.injectiontracker.ui.theme.Primary
import com.deeeelay.injectiontracker.ui.theme.SelectStroke

enum class SilhouetteMode { Display, Select }

private const val VIEWPORT_W = 140f
private const val VIEWPORT_H = 220f
private const val DISPLAY_DOT_RADIUS = 1.75f
private const val SELECT_DOT_RADIUS = 1.5f
private val MinHitTarget = 48.dp

@Composable
fun InjectionSilhouette(
    modifier: Modifier = Modifier,
    mode: SilhouetteMode,
    markedSite: InjectionSite? = null,
    onSiteSelected: ((InjectionSite) -> Unit)? = null,
) {
    val path = remember {
        PathParser().parsePathString(SILHOUETTE_PATH_DATA).toPath()
    }
    val stroke = if (mode == SilhouetteMode.Display) DisplayStroke else SelectStroke
    val dotRadius = if (mode == SilhouetteMode.Display) DISPLAY_DOT_RADIUS else SELECT_DOT_RADIUS
    val density = LocalDensity.current

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
            SilhouetteCanvas(
                modifier = Modifier.fillMaxSize(),
                path = path,
                stroke = stroke,
                markedSite = markedSite,
                dotRadiusViewport = dotRadius,
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
private fun SilhouetteCanvas(
    modifier: Modifier,
    path: Path,
    stroke: Color,
    markedSite: InjectionSite?,
    dotRadiusViewport: Float,
) {
    Canvas(modifier) {
        val sx = size.width / VIEWPORT_W
        val sy = size.height / VIEWPORT_H
        scale(sx, sy, pivot = Offset.Zero) {
            drawPath(path = path, color = BodyFill)
            drawPath(
                path = path,
                color = stroke,
                style = Stroke(
                    width = 1.25f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
            if (markedSite != null) {
                drawCircle(
                    color = Primary,
                    radius = dotRadiusViewport,
                    center = Offset(markedSite.viewportX, markedSite.viewportY),
                )
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
