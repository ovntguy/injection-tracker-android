package com.deeeelay.injectiontracker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onEditRegimen: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.settings_reminders),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_day_before)) },
            supportingContent = { Text(stringResource(R.string.settings_day_before_support)) },
            trailingContent = {
                Switch(
                    checked = state.dayBeforeEnabled,
                    onCheckedChange = viewModel::setDayBefore,
                )
            },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_at_time)) },
            supportingContent = { Text(stringResource(R.string.settings_at_time_support)) },
            trailingContent = {
                Switch(
                    checked = state.atTimeEnabled,
                    onCheckedChange = viewModel::setAtTime,
                )
            },
        )
        Text(
            stringResource(R.string.settings_regimen),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            OutlinedButton(onClick = onEditRegimen, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_edit_regimen))
            }
        }
    }
}
