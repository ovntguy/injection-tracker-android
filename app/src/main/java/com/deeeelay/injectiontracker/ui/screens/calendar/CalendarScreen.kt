package com.deeeelay.injectiontracker.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.ui.components.Formatters
import com.deeeelay.injectiontracker.ui.silhouette.siteLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthHeader(
            month = state.month,
            onPrev = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
        )
        MonthGrid(
            month = state.month,
            selected = state.selectedDate,
            marks = state.marksByDate,
            onSelect = viewModel::selectDate,
        )
        Text(
            stringResource(R.string.calendar_legend),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SelectedDayList(state.selectedEntries)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
        }
        Text(
            text = Formatters.monthTitle(month),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    marks: Map<LocalDate, DayMarks>,
    onSelect: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    // Sunday-first grid: Java DayOfWeek Monday=1 ... Sunday=7.
    val sundayFirstOffset = first.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val weekdayNames = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )

    Column {
        Row(Modifier.fillMaxWidth()) {
            weekdayNames.forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        var cell = 0
        val totalCells = ((sundayFirstOffset + daysInMonth + 6) / 7) * 7
        while (cell < totalCells) {
            Row(Modifier.fillMaxWidth()) {
                repeat(7) {
                    val dayIndex = cell - sundayFirstOffset + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = month.atDay(dayIndex)
                        DayCell(
                            date = date,
                            selected = date == selected,
                            marks = marks[date],
                            onSelect = { onSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    }
                    cell++
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    marks: DayMarks?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = date == LocalDate.now()
    val bg = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        today -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .clickable(onClick = onSelect)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (today) FontWeight.Bold else FontWeight.Normal,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (marks?.hasCompleted == true) {
                MarkerCircle(filled = true, color = MaterialTheme.colorScheme.primary)
            }
            if (marks?.hasMissed == true) {
                MarkerDiamond(color = MaterialTheme.colorScheme.secondary)
            }
            if (marks?.hasUpcoming == true) {
                MarkerCircle(filled = false, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MarkerCircle(filled: Boolean, color: Color) {
    Box(
        Modifier
            .size(8.dp)
            .drawBehind {
                if (filled) {
                    drawCircle(color)
                } else {
                    drawCircle(color, style = Stroke(width = size.minDimension * 0.22f))
                }
            },
    )
}

@Composable
private fun MarkerDiamond(color: Color) {
    Box(
        Modifier
            .size(8.dp)
            .drawBehind {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height / 2f)
                    close()
                }
                drawPath(path, color, style = Fill)
            },
    )
}

@Composable
private fun SelectedDayList(entries: List<CalendarEntry>) {
    if (entries.isEmpty()) {
        Text(
            stringResource(R.string.calendar_empty_day),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEach { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (entry) {
                        is CalendarEntry.Completed -> {
                            Text(stringResource(R.string.calendar_completed), style = MaterialTheme.typography.titleMedium)
                            Text(Formatters.dateTime(entry.at))
                            entry.log.site?.let { Text(siteLabel(it)) }
                        }
                        is CalendarEntry.Missed -> {
                            Text(stringResource(R.string.calendar_missed), style = MaterialTheme.typography.titleMedium)
                            Text(Formatters.dateTime(entry.at))
                        }
                        is CalendarEntry.Upcoming -> {
                            Text(stringResource(R.string.calendar_upcoming), style = MaterialTheme.typography.titleMedium)
                            Text(Formatters.dateTime(entry.at))
                        }
                    }
                }
            }
        }
    }
}
