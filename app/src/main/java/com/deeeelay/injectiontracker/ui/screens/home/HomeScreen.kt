package com.deeeelay.injectiontracker.ui.screens.home

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.NextShotRelative
import com.deeeelay.injectiontracker.domain.ZoneReadout
import com.deeeelay.injectiontracker.ui.components.Formatters
import com.deeeelay.injectiontracker.ui.components.nextShotRelativeText
import com.deeeelay.injectiontracker.ui.components.frequencyLabel
import com.deeeelay.injectiontracker.ui.silhouette.InjectionSilhouette
import com.deeeelay.injectiontracker.ui.silhouette.SilhouetteMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    remindersWanted: Boolean,
    onLogInjection: () -> Unit,
    onEditRegimen: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMissed by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val missedMessage = stringResource(R.string.home_marked_missed)
    // Item 16: the relative "Next shot" line refreshes on resume and every minute while visible.
    val now = rememberHomeNow()
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(areNotificationsEnabled(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationsAllowed = granted || areNotificationsEnabled(context)
        if (!granted) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                },
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = areNotificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            now = now,
            remindersWanted = remindersWanted,
            notificationsAllowed = notificationsAllowed,
            onLogInjection = onLogInjection,
            onMarkMissed = { showMissed = true },
            onEditRegimen = onEditRegimen,
            onEnableNotifications = {
                if (Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            },
                        )
                    }
                } else {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        },
                    )
                }
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showMissed) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMissed = false },
            sheetState = sheetState,
        ) {
            MissedSheetContent(
                onConfirm = {
                    viewModel.logMissed()
                    showMissed = false
                    scope.launch { snackbar.showSnackbar(missedMessage) }
                },
                onCancel = { showMissed = false },
            )
        }
    }
}

/** Item 18 sheet: title `Mark as missed?`, confirm `Mark missed`. */
@Composable
internal fun MissedSheetContent(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.missed_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.missed_body), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_mark_missed))
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_cancel))
        }
    }
}

@Composable
fun HomeContent(
    state: HomeUiState,
    remindersWanted: Boolean,
    notificationsAllowed: Boolean,
    onLogInjection: () -> Unit,
    onMarkMissed: () -> Unit,
    onEditRegimen: () -> Unit,
    onEnableNotifications: () -> Unit,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
) {
    HomeFitColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        top = {
            Text(state.medicineName, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(
                    R.string.home_dose_frequency,
                    state.dosage,
                    frequencyLabel(state.frequency),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Item 16: no regimen yet means no card (setup flow owns that state).
            state.nextInjectionAt?.let { due ->
                NextShotCard(due = due, now = now, zone = zone)
            }
            Text(
                stringResource(R.string.home_last_site),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        figure = { figureHeight ->
            LastSiteRow(state = state, now = now, zone = zone, figureHeight = figureHeight)
        },
        bottom = {
            Button(onClick = onLogInjection, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_log_injection))
            }
            OutlinedButton(onClick = onMarkMissed, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_mark_as_missed))
            }
            TextButton(onClick = onEditRegimen, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_edit_regimen))
            }
            if (remindersWanted && !notificationsAllowed) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.home_notif_denied),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        TextButton(
                            onClick = onEnableNotifications,
                        ) { Text(stringResource(R.string.action_enable)) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        },
    )
}

/**
 * Lays out Home so the last-site figure takes leftover height, clamped to 88–336 dp.
 * Scrolls only when leftover space is under 88 dp (Design polish spec, rev 2026-11-15).
 */
@Composable
private fun HomeFitColumn(
    modifier: Modifier = Modifier,
    gap: Dp = 16.dp,
    top: @Composable ColumnScope.() -> Unit,
    figure: @Composable (Dp) -> Unit,
    bottom: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier) { constraints ->
        val maxW = constraints.maxWidth
        val maxH = constraints.maxHeight
        val loose = Constraints(minWidth = 0, maxWidth = maxW, minHeight = 0, maxHeight = Constraints.Infinity)
        val topP = subcompose("top") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap), content = top)
        }.map { it.measure(loose) }
        val bottomP = subcompose("bottom") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap), content = bottom)
        }.map { it.measure(loose) }
        val gapPx = with(density) { gap.roundToPx() }
        val leftoverDp = (maxH - topP.sumOf { it.height } - bottomP.sumOf { it.height } - 2 * gapPx) / density.density
        val figureDp = HomeFigureLayout.figureHeight(leftoverDp)
        val scrolls = HomeFigureLayout.scrolls(leftoverDp)
        val figurePx = with(density) { figureDp.dp.roundToPx() }

        if (!scrolls) {
            val figP = subcompose("figure") { figure(figureDp.dp) }.map {
                it.measure(Constraints(minWidth = maxW, maxWidth = maxW, minHeight = figurePx, maxHeight = figurePx))
            }
            layout(maxW, maxH) {
                var y = 0
                topP.forEach { it.placeRelative(0, y); y += it.height }
                y += gapPx
                figP.forEach { it.placeRelative(0, y); y += it.height }
                y += gapPx
                bottomP.forEach { it.placeRelative(0, y) }
            }
        } else {
            val scrollP = subcompose("scroll") {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    top()
                    figure(HomeFigureLayout.MIN_DP.dp)
                    bottom()
                }
            }.map { it.measure(Constraints(minWidth = maxW, maxWidth = maxW, minHeight = 0, maxHeight = maxH)) }
            layout(maxW, maxH) { scrollP.forEach { it.placeRelative(0, 0) } }
        }
    }
}

@Composable
internal fun NextShotCard(due: Instant, now: Instant, zone: ZoneId) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.home_next_shot),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Formatters.heroDateTime(due, zone),
                style = MaterialTheme.typography.titleLarge,
            )
            val relative = NextShotRelative.of(due, now, zone)
            Text(
                text = nextShotRelativeText(relative),
                style = MaterialTheme.typography.bodyMedium,
                color = if (relative.overdue) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/**
 * Item 6: figure on the left half, the last-site label on the right half (vertically centered,
 * start-aligned). Missed logs never change the label, date or marker.
 */
@Composable
private fun LastSiteRow(state: HomeUiState, now: Instant, zone: ZoneId, figureHeight: Dp) {
    val site = state.lastSite
    val at = state.lastSiteAt
    val label = site?.let { ZoneReadout.label(it) }
    val date = at?.let { Formatters.shortDate(it, now, zone) }
    val description = if (label != null && date != null) {
        stringResource(R.string.home_last_site_description, label, date)
    } else {
        stringResource(R.string.home_no_shots_yet)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val compact = HomeFigureLayout.compact(figureHeight.value)
        InjectionSilhouette(
            modifier = Modifier
                .weight(1f)
                .height(figureHeight),
            mode = SilhouetteMode.Display,
            markedSite = site,
            description = description,
            homeMarkerDiameterDp = HomeFigureLayout.markerDiameterDp(figureHeight.value),
            hideSideLabels = compact,
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics {},
            horizontalAlignment = Alignment.Start,
        ) {
            if (label != null && date != null) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    stringResource(R.string.home_no_shots_yet),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}



/**
 * Clock for the relative next-shot line (item 16). Refreshes on lifecycle
 * ON_RESUME and again at the start of every minute while Home is visible.
 */
@Composable
fun rememberHomeNow(): Instant {
    var now by remember { mutableStateOf(Instant.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) now = Instant.now()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
            now = Instant.now()
        }
    }
    return now
}

private fun areNotificationsEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (!manager.areNotificationsEnabled()) return false
    if (Build.VERSION.SDK_INT >= 33) {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
    return true
}
