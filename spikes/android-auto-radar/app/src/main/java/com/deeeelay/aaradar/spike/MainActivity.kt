package com.deeeelay.aaradar.spike

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/** Phone launcher so the sideload is visible. Radar itself is the Android Auto session. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).apply {
                val pad = (24 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                textSize = 18f
                text = "Big Dawg radar spike. Open it from Android Auto (Desktop Head Unit or the car). This build draws the latest reflectivity frame and its scan time."
            },
        )
    }
}
