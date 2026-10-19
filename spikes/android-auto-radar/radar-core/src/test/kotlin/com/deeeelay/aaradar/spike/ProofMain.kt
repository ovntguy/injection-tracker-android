package com.deeeelay.aaradar.spike

import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

/**
 * Network proof for the spike. Run with `:radar-core:renderProof`.
 * Writes PNGs through [RadarCompositor], the same pixel path the car surface uses.
 */
object ProofMain {
    private val scanFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

    @JvmStatic
    fun main(args: Array<String>) {
        val docs = Path.of("docs")
        Files.createDirectories(docs)
        val load = RadarFrameLoader().loadNearest(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        val frame = load.frame
        val basemap = load.tile?.let { decodePng(it.png) }
        val bounds = Geodesy.boundsAround(frame.latitude, frame.longitude, frame.maxRangeKm)
        val composed = RadarCompositor.compose(
            frame = frame,
            width = 640,
            height = 640,
            bounds = bounds,
            basemap = basemap,
            basemapBounds = load.tile?.coord?.bounds,
            dark = true,
        )
        val label = "${load.site.id} ${frame.productName}  ${scanFormat.format(frame.scanTime)}"
        val localPath = docs.resolve("local-nexrad-reflectivity.png")
        writeAnnotated(composed, localPath, label, load.tile != null)

        val mrms = MrmsClient().fetchComposite(height = 480)
        val nationalPath = docs.resolve("national-mrms-reflectivity.png")
        Files.write(nationalPath, mrms.png)

        val echoPixels = composed.pixels.count { pixel ->
            val background = 0xFF101418.toInt()
            pixel != background && pixel != 0
        }
        val metadata = buildString {
            appendLine("{")
            appendLine("  \"localSite\": \"${load.site.id}\",")
            appendLine("  \"localName\": \"${load.site.name}\",")
            appendLine("  \"product\": \"${frame.productName}\",")
            appendLine("  \"productCode\": ${frame.productCode},")
            appendLine("  \"scanTime\": \"${frame.scanTime}\",")
            appendLine("  \"latitude\": ${frame.latitude},")
            appendLine("  \"longitude\": ${frame.longitude},")
            appendLine("  \"gateKm\": ${frame.gateKm},")
            appendLine("  \"binCount\": ${frame.binCount},")
            appendLine("  \"radials\": ${frame.radials.size},")
            appendLine("  \"sourceUrl\": \"${frame.sourceUrl}\",")
            appendLine("  \"osmTile\": \"${load.tile?.coord?.url ?: ""}\",")
            appendLine("  \"osmError\": ${jsonString(load.tileError)},")
            appendLine("  \"echoPixels\": $echoPixels,")
            appendLine("  \"mrmsLayer\": \"${mrms.layerName}\",")
            appendLine("  \"mrmsCapabilities\": \"${mrms.capabilitiesUrl}\",")
            appendLine("  \"mrmsUrlTemplate\": ${jsonString(mrms.urlTemplate)},")
            appendLine("  \"mrmsRequestUrl\": ${jsonString(mrms.requestUrl)},")
            appendLine("  \"mrmsTime\": ${jsonString(mrms.time)},")
            appendLine("  \"mrmsTimeCount\": ${mrms.timeCount},")
            appendLine("  \"mrmsPngBytes\": ${mrms.png.size}")
            appendLine("}")
        }
        Files.writeString(docs.resolve("proof-metadata.json"), metadata)
        println(metadata)
        check(echoPixels > 100) { "Local render has too few echo pixels ($echoPixels)" }
    }

    private fun jsonString(value: String?): String {
        if (value == null) {
            return "null"
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    private fun decodePng(bytes: ByteArray): ArgbImage {
        val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: error("OSM tile was not a PNG")
        val pixels = IntArray(image.width * image.height)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                pixels[y * image.width + x] = image.getRGB(x, y)
            }
        }
        return ArgbImage(image.width, image.height, pixels)
    }

    private fun writeAnnotated(image: ArgbImage, path: Path, label: String, hasBasemap: Boolean) {
        val buffered = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        buffered.setRGB(0, 0, image.width, image.height, image.pixels, 0, image.width)
        val graphics = buffered.createGraphics()
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 22)
        graphics.color = Color(0, 0, 0, 160)
        graphics.fillRect(12, image.height - 78, image.width - 24, 62)
        graphics.color = Color.WHITE
        graphics.drawString(label, 24, image.height - 48)
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 16)
        val credit = if (hasBasemap) {
            "Basemap © OpenStreetMap contributors"
        } else {
            "Basemap tile unavailable"
        }
        graphics.drawString(credit, 24, image.height - 24)
        graphics.dispose()
        ImageIO.write(buffered, "png", path.toFile())
    }
}
