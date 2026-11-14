package com.deeeelay.injectiontracker.ui.screens.logdone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.ZoneReadout
import com.deeeelay.injectiontracker.ui.components.DateTimeFields
import com.deeeelay.injectiontracker.ui.silhouette.InjectionSilhouette
import com.deeeelay.injectiontracker.ui.silhouette.SilhouetteMode
import com.deeeelay.injectiontracker.ui.silhouette.ZoneGeometry
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime

private val ScreenPadding = 24.dp
private val HitPadding = ZoneGeometry.HIT_PADDING_DP.dp
private val ReadoutBottom = 24.dp

@Composable
fun LogDoneScreen(
    viewModel: LogDoneViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }
    LogDoneContent(
        state = state,
        onBack = onBack,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onSiteSelected = viewModel::selectSite,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDoneContent(
    state: LogDoneState,
    onBack: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onSiteSelected: (InjectionSite) -> Unit,
    onSave: () -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_done_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            // Save log is pinned so it stays reachable when the figure makes the content scroll.
            Surface(color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                ) {
                    Text(stringResource(R.string.action_save_log))
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val viewportHeight = maxHeight
            FigureFillColumn(
                viewportHeight = viewportHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding),
                top = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DateTimeFields(
                            date = state.date,
                            time = state.time,
                            onDateChange = onDateChange,
                            onTimeChange = onTimeChange,
                        )
                        Text(
                            stringResource(R.string.log_done_tap_site),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                figure = { availableHeight ->
                    val scale = ZoneGeometry.logFigureScale(availableHeight.value)
                    val crop = ZoneGeometry.LOG_BODY_CROP
                    InjectionSilhouette(
                        modifier = Modifier.size(
                            width = (crop.width * scale).dp + HitPadding * 2,
                            height = (crop.height * scale).dp + HitPadding * 2,
                        ),
                        window = crop,
                        mode = SilhouetteMode.Select,
                        markedSite = state.site,
                        lastSite = state.lastSite,
                        hitPadding = HitPadding,
                        onSiteSelected = onSiteSelected,
                    )
                },
                bottom = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // 16 dp above the readout comes from the figure's hit padding.
                        ZoneReadoutBlock(state, zone)
                        Spacer(Modifier.height(ReadoutBottom))
                    }
                },
            )
        }
    }
}

/**
 * Lays out [top], the figure and [bottom], giving the figure the vertical space left in
 * [viewportHeight] (Design: fill between the prompt and the readout). The figure may make
 * the column taller than the viewport, in which case the parent scrolls.
 */
@Composable
private fun FigureFillColumn(
    viewportHeight: Dp,
    modifier: Modifier,
    top: @Composable () -> Unit,
    figure: @Composable (availableHeight: Dp) -> Unit,
    bottom: @Composable () -> Unit,
) {
    androidx.compose.ui.layout.SubcomposeLayout(modifier) { constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity)
        val topP = subcompose("top", top).map { it.measure(loose) }
        val bottomP = subcompose("bottom", bottom).map { it.measure(loose) }
        // The figure's 16 dp hit padding provides the visual gap above and below it.
        val gap = 0
        val used = topP.sumOf { it.height } + bottomP.sumOf { it.height } + gap
        val availablePx = (viewportHeight.roundToPx() - used).coerceAtLeast(0)
        // The figure box carries 16 dp hit padding on each side; give the viewBox the rest.
        val availableFigure = (availablePx - (HitPadding * 2).roundToPx()).coerceAtLeast(0).toDp()
        val figP = subcompose("figure") { figure(availableFigure) }.map { it.measure(loose) }
        val width = constraints.maxWidth
        val height = topP.sumOf { it.height } + gap + figP.sumOf { it.height } + bottomP.sumOf { it.height }
        layout(width, height) {
            var y = 0
            topP.forEach { it.placeRelative(0, y); y += it.height }
            y += gap
            figP.forEach { it.placeRelative((width - it.width) / 2, y); y += it.height }
            bottomP.forEach { it.placeRelative(0, y); y += it.height }
        }
    }
}

private val ReadoutDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
private fun ZoneReadoutBlock(state: LogDoneState, zone: ZoneId) {
    val selected = state.site
    if (selected != null) {
        Text(
            ZoneReadout.label(selected),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    } else {
        Text(
            stringResource(R.string.log_readout_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    val last = state.lastSite
    val lastAt = state.lastSiteAt
    if (last != null && lastAt != null) {
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LastSiteLegendGlyph()
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(
                    R.string.log_readout_last_time,
                    ZoneReadout.label(last),
                    ReadoutDate.format(lastAt.atZone(zone)),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 12 dp copy of the last-site glyph (ring + center dot); doubles as the legend. */
@Composable
private fun LastSiteLegendGlyph() {
    val ring = MaterialTheme.colorScheme.outline
    val dot = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(12.dp)) {
        val stroke = 1.5.dp.toPx() * 12f / ZoneGeometry.REST_RING_DP
        drawCircle(ring, size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
        drawCircle(dot, ZoneGeometry.LAST_SITE_DOT_DP.dp.toPx() / 2 * 12f / ZoneGeometry.REST_RING_DP)
    }
}

