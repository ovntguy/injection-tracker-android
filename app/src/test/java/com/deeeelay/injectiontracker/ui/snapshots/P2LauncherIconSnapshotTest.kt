package com.deeeelay.injectiontracker.ui.snapshots

import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Design Critique screenshots for P2: the adaptive launcher icon (full color, circle and
 * squircle-ish masks) and the themed (monochrome) icon on light and dark launcher surfaces.
 */
@RunWith(Parameterized::class)
class P2LauncherIconSnapshotTest(private val night: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun modes() = listOf(false, true)
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(
            nightMode = if (night) NightMode.NIGHT else NightMode.NOTNIGHT,
            screenHeight = 1400,
        ),
        maxPercentDifference = 0.0,
    )

    private val mode get() = if (night) "dark" else "light"

    @Test
    fun launcherIcon() {
        val ctx = paparazzi.context
        val px = (ctx.resources.displayMetrics.density * 64).toInt()
        // Material 3 themed-icon pairs from the #6750A4 seed.
        val (themedBg, themedFg) = if (night) 0xFF4F378B.toInt() to 0xFFEADDFF.toInt() else 0xFFEADDFF.toInt() to 0xFF21005D.toInt()
        val wallpaper = if (night) 0xFF141218.toInt() else 0xFFFEF7FF.toInt()

        fun label(text: String) = TextView(ctx).apply {
            this.text = text
            setTextColor(if (night) 0xFFE6E0E9.toInt() else 0xFF1D1B20.toInt())
            textSize = 14f
            setPadding(0, 8, 0, 24)
        }

        /** Masked adaptive icon: 108dp layers shown through the centered 72dp viewport. */
        fun masked(fg: Int, bgColor: Int?, bgRes: Int?, tint: Int?, round: Boolean): FrameLayout {
            val layer = (px * 1.5f).toInt()
            return FrameLayout(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(px, px)
                clipToOutline = true
                outlineProvider = object : android.view.ViewOutlineProvider() {
                    override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                        if (round) {
                            outline.setOval(0, 0, view.width, view.height)
                        } else {
                            outline.setRoundRect(0, 0, view.width, view.height, view.width * 0.25f)
                        }
                    }
                }
                bgColor?.let { setBackgroundColor(it) }
                fun add(res: Int, tintColor: Int?) = addView(
                    ImageView(ctx).apply {
                        setImageDrawable(ctx.getDrawable(res)!!.mutate().apply { tintColor?.let { c -> setTint(c) } })
                    },
                    FrameLayout.LayoutParams(layer, layer).apply {
                        leftMargin = (px - layer) / 2
                        topMargin = (px - layer) / 2
                    },
                )
                bgRes?.let { add(it, null) }
                add(fg, tint)
            }
        }

        /** Full 108dp foreground layer with the 66dp safe-zone circle outlined. */
        fun safeZone(): FrameLayout = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(px, px)
            addView(ImageView(ctx).apply { setImageResource(R.drawable.ic_launcher_background) }, FrameLayout.LayoutParams(px, px))
            addView(ImageView(ctx).apply { setImageResource(R.drawable.ic_launcher_foreground) }, FrameLayout.LayoutParams(px, px))
            val safe = (px * 66f / 108f).toInt()
            addView(
                android.view.View(ctx).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setStroke(3, 0xFFFFD54F.toInt())
                    }
                },
                FrameLayout.LayoutParams(safe, safe).apply {
                    leftMargin = (px - safe) / 2
                    topMargin = (px - safe) / 2
                },
            )
        }

        fun icon(round: Boolean) =
            masked(R.drawable.ic_launcher_foreground, null, R.drawable.ic_launcher_background, null, round)

        fun themed() = masked(R.drawable.ic_launcher_monochrome, themedBg, null, themedFg, round = true)

        fun row(vararg views: android.view.View) = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            views.forEach { v ->
                addView(v)
                addView(android.widget.Space(ctx), LinearLayout.LayoutParams(px / 3, 1))
            }
        }

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(wallpaper)
            setPadding(48, 48, 48, 48)
            addView(label("Adaptive icon: ic_launcher / ic_launcher_round"))
            addView(row(icon(round = false), icon(round = true)))
            addView(label("Themed icon (monochrome layer, Android 13+)"))
            addView(row(themed()))
            addView(label("Full 108dp layers, yellow = 66dp safe zone"))
            addView(row(safeZone()))
        }
        paparazzi.snapshot(root, name = "launcher_icon_$mode")
    }
}
