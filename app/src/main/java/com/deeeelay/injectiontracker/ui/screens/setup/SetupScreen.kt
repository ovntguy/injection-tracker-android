package com.deeeelay.injectiontracker.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.ui.components.DateTimeFields
import com.deeeelay.injectiontracker.ui.components.FrequencyDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onSaved: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.setup_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.medicineName,
                onValueChange = viewModel::updateName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_medicine_name)) },
                isError = state.nameError,
                supportingText = if (state.nameError) {
                    { Text(stringResource(R.string.error_required)) }
                } else {
                    null
                },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.dosage,
                onValueChange = viewModel::updateDosage,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_dosage)) },
                isError = state.dosageError,
                supportingText = if (state.dosageError) {
                    { Text(stringResource(R.string.error_required)) }
                } else {
                    null
                },
                singleLine = true,
            )
            FrequencyDropdown(
                value = state.frequency,
                onChange = viewModel::updateFrequency,
            )
            DateTimeFields(
                date = state.date,
                time = state.time,
                onDateChange = viewModel::updateDate,
                onTimeChange = viewModel::updateTime,
                dateLabel = stringResource(R.string.field_start_date),
                timeLabel = stringResource(R.string.field_start_time),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save_and_continue))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
