package com.deeeelay.aaradar.spike

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Header
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapController
import androidx.car.app.navigation.model.MapWithContentTemplate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

/**
 * MapWithContentTemplate (Car API 7+) with the reflectivity frame drawn on the surface.
 * Looping while parked is a product rule and is not implemented in this spike.
 */
class RadarScreen(carContext: CarContext) : Screen(carContext) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private val scanFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)
    private val state = RadarDrawState()
    private val surface = RadarSurface(carContext, state)
    private var callbackInstalled = false

    init {
        installCallback()
        state.status = "Loading nearest NEXRAD reflectivity…"
        io.execute { loadFrame() }
    }

    override fun onGetTemplate(): Template {
        installCallback()
        val dark = carContext.isDarkMode
        state.dark = dark
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle(state.title)
                    .addText(state.status)
                    .build(),
            )
            .build()
        val content = PaneTemplate.Builder(pane)
            .setHeader(
                Header.Builder()
                    .setTitle("Reflectivity")
                    .build(),
            )
            .build()
        return MapWithContentTemplate.Builder()
            .setContentTemplate(content)
            .setMapController(MapController.Builder().build())
            .build()
    }

    private fun installCallback() {
        if (callbackInstalled) {
            return
        }
        callbackInstalled = true
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surface)
    }

    private fun loadFrame() {
        try {
            val load = RadarFrameLoader().loadNearest(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
            val basemap = load.tile?.let { decodeTile(it.png) }
            val bounds = Geodesy.boundsAround(load.frame.latitude, load.frame.longitude, load.frame.maxRangeKm)
            val image = RadarCompositor.compose(
                frame = load.frame,
                width = 640,
                height = 640,
                bounds = bounds,
                basemap = basemap,
                basemapBounds = load.tile?.coord?.bounds,
                dark = state.dark,
            )
            state.image = image
            state.title = "${load.site.id} ${load.frame.productName}"
            state.status = "Scan ${scanFormat.format(load.frame.scanTime)}"
            state.canvasLabel = "${state.title}  ${state.status}"
        } catch (error: Exception) {
            state.title = "Reflectivity"
            state.status = error.message?.take(80) ?: "Radar fetch failed"
            state.canvasLabel = state.status
            state.image = null
        }
        mainHandler.post {
            invalidate()
            surface.redraw()
        }
    }

    private fun decodeTile(png: ByteArray): ArgbImage {
        val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size)
            ?: error("OSM tile was not a PNG")
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()
        return ArgbImage(width, height, pixels)
    }
}

private class RadarDrawState {
    @Volatile var image: ArgbImage? = null
    @Volatile var title: String = "Reflectivity"
    @Volatile var status: String = "Loading nearest NEXRAD reflectivity…"
    @Volatile var canvasLabel: String = status
    @Volatile var dark: Boolean = true
}

private class RadarSurface(
    private val carContext: CarContext,
    private val state: RadarDrawState,
) : SurfaceCallback {
    @Volatile private var container: SurfaceContainer? = null

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        container = surfaceContainer
        redraw()
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        container = null
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        redraw()
    }

    fun redraw() {
        val surface = container?.surface ?: return
        val canvas = surface.lockCanvas(null) ?: return
        try {
            draw(canvas)
        } finally {
            surface.unlockCanvasAndPost(canvas)
        }
    }

    private fun draw(canvas: Canvas) {
        val dark = carContext.isDarkMode
        canvas.drawColor(if (dark) Color.rgb(16, 20, 24) else Color.rgb(231, 238, 242))
        val image = state.image
        if (image != null) {
            val bitmap = Bitmap.createBitmap(image.pixels, image.width, image.height, Bitmap.Config.ARGB_8888)
            canvas.drawBitmap(bitmap, null, Rect(0, 0, canvas.width, canvas.height), null)
            bitmap.recycle()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dark) Color.WHITE else Color.BLACK
            textSize = (canvas.height / 28f).coerceAtLeast(18f)
        }
        val label = state.canvasLabel
        val y = canvas.height - paint.textSize
        canvas.drawText(label, 16f, y, paint)
    }
}
