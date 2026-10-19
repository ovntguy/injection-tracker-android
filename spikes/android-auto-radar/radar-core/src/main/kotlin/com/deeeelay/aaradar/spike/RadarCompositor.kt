package com.deeeelay.aaradar.spike

class ArgbImage(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
) {
    init {
        require(pixels.size == width * height) {
            "pixel count ${pixels.size} != $width x $height"
        }
    }

    fun pixel(x: Int, y: Int): Int = pixels[y * width + x]
}

object ReflectivityColors {
    private val stops = doubleArrayOf(5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 75.0)
    private val colors = intArrayOf(
        0xCC04E9E7.toInt(),
        0xCC019FF4.toInt(),
        0xCC0300F4.toInt(),
        0xCC02FD02.toInt(),
        0xCC01C501.toInt(),
        0xCC008E00.toInt(),
        0xCCFDF802.toInt(),
        0xCCE5BC00.toInt(),
        0xCCFD9500.toInt(),
        0xCCFD0000.toInt(),
        0xCCD40000.toInt(),
        0xCCBC0000.toInt(),
        0xCCF800FD.toInt(),
        0xCC9854C6.toInt(),
    )

    fun argb(dbz: Double): Int {
        if (dbz < stops[0]) {
            return 0
        }
        var index = 0
        while (index < stops.size - 1 && dbz >= stops[index + 1]) {
            index += 1
        }
        return colors[index]
    }
}

object RadarCompositor {
    fun compose(
        frame: ReflectivityFrame,
        width: Int,
        height: Int,
        bounds: GeoBounds = Geodesy.boundsAround(frame.latitude, frame.longitude, frame.maxRangeKm),
        basemap: ArgbImage? = null,
        basemapBounds: GeoBounds? = null,
        dark: Boolean = true,
    ): ArgbImage {
        val background = if (dark) 0xFF101418.toInt() else 0xFFE7EEF2.toInt()
        val pixels = IntArray(width * height) { background }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val (lat, lon) = WebMercator.lonLatAt(bounds, x + 0.5, y + 0.5, width, height)
                var color = basemapColor(basemap, basemapBounds, lat, lon) ?: background
                val (rangeKm, azimuth) = Geodesy.rangeAzimuthKm(frame.latitude, frame.longitude, lat, lon)
                val raw = frame.sampleRaw(azimuth, rangeKm)
                val dbz = frame.dbz(raw)
                if (dbz != null) {
                    val radar = ReflectivityColors.argb(dbz)
                    if (radar != 0) {
                        color = over(color, radar)
                    }
                }
                pixels[y * width + x] = color
            }
        }
        return ArgbImage(width, height, pixels)
    }

    private fun basemapColor(basemap: ArgbImage?, bounds: GeoBounds?, latitude: Double, longitude: Double): Int? {
        if (basemap == null || bounds == null) {
            return null
        }
        if (longitude < bounds.west || longitude > bounds.east || latitude < bounds.south || latitude > bounds.north) {
            return null
        }
        val xFrac = (longitude - bounds.west) / (bounds.east - bounds.west)
        val yNorth = WebMercator.mercatorY(bounds.north)
        val ySouth = WebMercator.mercatorY(bounds.south)
        val projected = WebMercator.mercatorY(latitude)
        val yFrac = (yNorth - projected) / (yNorth - ySouth)
        val x = (xFrac * basemap.width).toInt().coerceIn(0, basemap.width - 1)
        val y = (yFrac * basemap.height).toInt().coerceIn(0, basemap.height - 1)
        return basemap.pixel(x, y)
    }

    private fun over(dst: Int, src: Int): Int {
        val srcA = (src ushr 24) and 0xFF
        if (srcA == 0) {
            return dst
        }
        if (srcA == 255) {
            return src
        }
        val dstA = (dst ushr 24) and 0xFF
        val inv = 255 - srcA
        val outA = srcA + dstA * inv / 255
        if (outA == 0) {
            return 0
        }
        fun channel(srcC: Int, dstC: Int): Int = (srcC * srcA + dstC * dstA * inv / 255) / outA
        val r = channel((src ushr 16) and 0xFF, (dst ushr 16) and 0xFF)
        val g = channel((src ushr 8) and 0xFF, (dst ushr 8) and 0xFF)
        val b = channel(src and 0xFF, dst and 0xFF)
        return (outA shl 24) or (r shl 16) or (g shl 8) or b
    }
}
