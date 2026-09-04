package com.deeeelay.injectiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deeeelay.injectiontracker.ui.navigation.InjectionTrackerNav
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as InjectionTrackerApp
        setContent {
            InjectionTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InjectionTrackerNav(
                        repository = app.container.repository,
                        factory = app.container.viewModelFactory,
                    )
                }
            }
        }
    }
}
