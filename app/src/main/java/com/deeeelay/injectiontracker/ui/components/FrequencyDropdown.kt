package com.deeeelay.injectiontracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.Frequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDropdown(
    value: Frequency,
    onChange: (Frequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = frequencyLabel(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_frequency)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Frequency.entries.forEach { freq ->
                DropdownMenuItem(
                    text = { Text(frequencyLabel(freq)) },
                    onClick = {
                        onChange(freq)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun frequencyLabel(frequency: Frequency): String =
    stringResource(
        when (frequency) {
            Frequency.EVERY_WEEK -> R.string.frequency_every_week
            Frequency.EVERY_OTHER_WEEK -> R.string.frequency_every_other_week
        },
    )
