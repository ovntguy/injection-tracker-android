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
import com.deeeelay.injectiontracker.domain.NextShotRelative
import com.deeeelay.injectiontracker.domain.ZoneReadout
import com.deeeelay.injectiontracker.ui.components.nextShotRelativeText
import androidx.compose.material3.ListItem
import androidx.compose.ui.unit.Dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalendarContent(
        state = state,
        onPrev = viewModel::previousMonth,
        onNext = viewModel::nextMonth,
        onSelect = viewModel::selectDate,
    )
}

@Composable
fun CalendarContent(
    state: CalendarUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthHeader(month = state.month, onPrev = onPrev, onNext = onNext)
        MonthGrid(
            month = state.month,
            today = state.today,
            selected = state.selectedDate,
            marks = state.marksByDate,
            onSelect = onSelect,
        )
        CalendarLegend()
        state.selectedDate?.let { date ->
            DayDetail(date = date, entries = state.selectedEntries, today = state.today, now = now, zone = zone)
        }
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
    today: LocalDate,
    selected: LocalDate?,
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
                            today = date == today,
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
    today: Boolean,
    selected: Boolean,
    marks: DayMarks?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (today && !selected) scheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Item 10: the selected day gets an M3 filled date circle in primary.
        Box(
            modifier = Modifier
                .size(DateCircle)
                .then(if (selected) Modifier.background(scheme.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (today) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) scheme.onPrimary else scheme.onSurface,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (marks?.hasCompleted == true) DoneGlyph(CellGlyph)
            if (marks?.hasMissed == true) MissedGlyph(CellGlyph)
            if (marks?.hasUpcoming == true) UpcomingGlyph(CellGlyph)
        }
    }
}

private val DateCircle = 28.dp
private val CellGlyph = 8.dp
private val LegendGlyph = 12.dp

/** Same vectors in the cells, the legend and the detail rows (item 9). */
@Composable
internal fun DoneGlyph(size: Dp) {
    val color = MaterialTheme.colorScheme.primary
    Box(Modifier.size(size).drawBehind { drawCircle(color) })
}

@Composable
internal fun MissedGlyph(size: Dp) {
    val color = MaterialTheme.colorScheme.error
    Box(
        Modifier
            .size(size)
            .drawBehind {
                val path = Path().apply {
                    moveTo(this@drawBehind.size.width / 2f, 0f)
                    lineTo(this@drawBehind.size.width, this@drawBehind.size.height / 2f)
                    lineTo(this@drawBehind.size.width / 2f, this@drawBehind.size.height)
                    lineTo(0f, this@drawBehind.size.height / 2f)
                    close()
                }
                drawPath(path, color, style = Fill)
            },
    )
}

@Composable
internal fun UpcomingGlyph(size: Dp) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(size)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawCircle(color, radius = this.size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
            },
    )
}

/** Item 9: one centered row under the grid; 12 dp glyphs, 4 dp gap, 16 dp between items. */
@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(R.string.calendar_done) { DoneGlyph(LegendGlyph) }
        LegendItem(R.string.calendar_missed) { MissedGlyph(LegendGlyph) }
        LegendItem(R.string.calendar_upcoming) { UpcomingGlyph(LegendGlyph) }
    }
}

@Composable
private fun LegendItem(label: Int, glyph: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        glyph()
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Item 10: inline, read-only detail for the selected day. */
@Composable
private fun DayDetail(
    date: LocalDate,
    entries: List<CalendarEntry>,
    today: LocalDate,
    now: Instant,
    zone: ZoneId,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            CalendarFormat.dayHeader(date, today),
            style = MaterialTheme.typography.titleSmall,
        )
        if (entries.isEmpty()) {
            Text(
                stringResource(R.string.calendar_nothing_logged),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            return
        }
        entries.forEach { entry ->
            val time = Formatters.time(entry.at, zone)
            when (entry) {
                is CalendarEntry.Completed -> DetailRow(
                    glyph = { DoneGlyph(LegendGlyph) },
                    headline = entry.log.site?.let { ZoneReadout.label(it) } ?: stringResource(R.string.calendar_done),
                    supporting = stringResource(R.string.calendar_row_done, time),
                )
                is CalendarEntry.Missed -> DetailRow(
                    glyph = { MissedGlyph(LegendGlyph) },
                    headline = stringResource(R.string.calendar_missed),
                    supporting = stringResource(R.string.calendar_row_missed, time),
                )
                is CalendarEntry.Upcoming -> DetailRow(
                    glyph = { UpcomingGlyph(LegendGlyph) },
                    headline = stringResource(R.string.calendar_row_next_shot),
                    supporting = stringResource(
                        R.string.calendar_row_upcoming,
                        time,
                        nextShotRelativeText(NextShotRelative.of(entry.at, now, zone)),
                    ),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(glyph: @Composable () -> Unit, headline: String, supporting: String) {
    ListItem(
        leadingContent = glyph,
        headlineContent = { Text(headline, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

/** Header `EEEE, MMM d` (adds `, yyyy` outside the current year, per the spec's date rule). */
object CalendarFormat {
    private val header: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    private val headerWithYear: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

    fun dayHeader(date: LocalDate, today: LocalDate): String =
        (if (date.year == today.year) header else headerWithYear).format(date)
}
