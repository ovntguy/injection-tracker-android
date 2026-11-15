package com.deeeelay.injectiontracker.ui.screens.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.deeeelay.injectiontracker.ui.components.frequencyLabel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onEditRegimen: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(areNotificationsEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsAllowed = areNotificationsEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    SettingsContent(
        state = state,
        notificationsAllowed = notificationsAllowed,
        onDayBeforeChange = viewModel::setDayBefore,
        onAtTimeChange = viewModel::setAtTime,
        onEditRegimen = onEditRegimen,
        onTurnOnNotifications = {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        },
    )
}

/** Settings (Design polish spec, P5 items 12, 13, 17). Presentation only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    notificationsAllowed: Boolean,
    onDayBeforeChange: (Boolean) -> Unit,
    onAtTimeChange: (Boolean) -> Unit,
    onEditRegimen: () -> Unit,
    onTurnOnNotifications: () -> Unit,
) {
    val switches = ReminderSwitches.of(state, notificationsAllowed)
    Column(Modifier.fillMaxSize()) {
        // Item 12: small top bar, no nav icon. The tab Scaffold already pads for the status bar.
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            windowInsets = WindowInsets(0),
        )
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SectionLabel(R.string.settings_reminders)
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_day_before)) },
                supportingContent = {
                    Text(
                        stringResource(R.string.settings_day_before_support),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = switches.dayBefore,
                        onCheckedChange = onDayBeforeChange,
                        enabled = switches.enabled,
                    )
                },
            )
            ListItem(
                modifier = Modifier.heightIn(min = 56.dp),
                headlineContent = { Text(stringResource(R.string.settings_when_due)) },
                trailingContent = {
                    Switch(
                        checked = switches.atTime,
                        onCheckedChange = onAtTimeChange,
                        enabled = switches.enabled,
                    )
                },
            )
            if (!notificationsAllowed) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.settings_notifications_off),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        TextButton(onClick = onTurnOnNotifications) {
                            Text(stringResource(R.string.settings_turn_on))
                        }
                    },
                )
            }
            SectionLabel(R.string.settings_regimen)
            state.regimen?.let { regimen ->
                ListItem(
                    modifier = Modifier
                        .heightIn(min = 72.dp)
                        .clickable(onClick = onEditRegimen),
                    headlineContent = {
                        Text(
                            stringResource(R.string.settings_regimen_headline, regimen.medicineName, regimen.dosage),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            frequencyLabel(regimen.frequency),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
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
