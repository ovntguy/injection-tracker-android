package com.deeeelay.injectiontracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light scheme from the Material 3 baseline, which is the tonal scheme generated from seed
 * #6750A4 (dynamic color off). Only roles that differ from the baseline would be overridden here;
 * the old teal secondary family (#00897B / #B2DFDB) is gone so the nav indicator uses the
 * seed's secondaryContainer (Design polish spec, item 11).
 */
internal val LightColors = lightColorScheme(
    surfaceVariant = SurfaceVariant,
)

@Composable
fun InjectionTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
