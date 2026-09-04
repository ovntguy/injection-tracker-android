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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.ui.components.Formatters
import com.deeeelay.injectiontracker.ui.components.frequencyLabel
import com.deeeelay.injectiontracker.ui.silhouette.InjectionSilhouette
import com.deeeelay.injectiontracker.ui.silhouette.SilhouetteMode

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.nextInjectionAt?.let { Formatters.heroDateTime(it) }.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.overdue) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(stringResource(R.string.home_overdue)) },
                        )
                    }
                }
            }
        }

        Text(stringResource(R.string.home_last_site), style = MaterialTheme.typography.titleMedium)
        if (state.lastSite != null) {
            InjectionSilhouette(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                mode = SilhouetteMode.Display,
                markedSite = state.lastSite,
            )
        } else {
            Text(
                text = stringResource(R.string.home_last_site_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InjectionSilhouette(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                mode = SilhouetteMode.Display,
                markedSite = null,
            )
        }

        Button(onClick = onLogInjection, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_log_injection))
        }
        OutlinedButton(onClick = { showMissed = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_i_missed_it))
        }
        TextButton(onClick = onEditRegimen, modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
                        onClick = {
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
                    ) { Text(stringResource(R.string.action_enable)) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showMissed) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMissed = false },
            sheetState = sheetState,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.missed_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.missed_body), style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = {
                        viewModel.logMissed()
                        showMissed = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_log_missed)) }
                TextButton(
                    onClick = { showMissed = false },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_cancel)) }
            }
        }
    }
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
