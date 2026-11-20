package com.deeeelay.injectiontracker.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.deeeelay.injectiontracker.R

/** Tells the user a save didn't go through, so a tap on Save never fails silently. */
@Composable
fun SaveFailedToast(failed: Boolean, onShown: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(failed) {
        if (failed) {
            Toast.makeText(context, R.string.error_save_failed, Toast.LENGTH_LONG).show()
            onShown()
        }
    }
}
