package com.deeeelay.injectiontracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.NextShotRelative

/** Strings for [NextShotRelative] (Design polish spec, item 16). */
@Composable
fun nextShotRelativeText(relative: NextShotRelative): String = when (relative) {
    is NextShotRelative.InDays ->
        pluralStringResource(R.plurals.next_in_days, relative.days.toInt(), relative.days.toInt())
    NextShotRelative.Tomorrow -> stringResource(R.string.next_tomorrow)
    is NextShotRelative.TodayInHours -> stringResource(R.string.next_today_in_hours, relative.hours.toInt())
    is NextShotRelative.InMinutes -> stringResource(R.string.next_in_minutes, relative.minutes.toInt())
    NextShotRelative.DueNow -> stringResource(R.string.next_due_now)
    is NextShotRelative.OverdueByHours -> stringResource(R.string.next_overdue_by_hours, relative.hours.toInt())
    is NextShotRelative.OverdueByMinutes -> stringResource(R.string.next_overdue_by_minutes, relative.minutes.toInt())
    is NextShotRelative.DaysOverdue ->
        pluralStringResource(R.plurals.next_days_overdue, relative.days.toInt(), relative.days.toInt())
}
