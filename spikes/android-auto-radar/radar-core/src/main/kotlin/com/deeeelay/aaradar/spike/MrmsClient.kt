package com.deeeelay.aaradar.spike

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

data class WmsTimeDimension(
    val defaultTime: String?,
    val times: List<String>,
)

data class MrmsMapRequest(
    val layerName: String,
    val capabilitiesUrl: String,
    val urlTemplate: String,
    val requestUrl: String,
    val time: String?,
    val timeCount: Int,
    val png: ByteArray,
)

/**
 * CONUS MRMS quality-controlled composite reflectivity.
 * Layer chosen from https://opengeo.ncep.noaa.gov/geoserver/www/ (CREF QCD column).
 */
class MrmsClient(
    private val fetch: (String) -> ByteArray = ::httpGet,
) {
    fun fetchComposite(width: Int = 0, height: Int = 480): MrmsMapRequest {
        val capabilities = fetch(CAPABILITIES_URL)
        val dimension = parseTimeDimension(capabilities)
        val time = dimension.defaultTime ?: dimension.times.lastOrNull()
        val box = conusBox()
        val pixelWidth = if (width > 0) width else (height * box.aspect).toInt().coerceAtLeast(1)
        val requestUrl = getMapUrl(time, box, pixelWidth, height)
        val png = fetch(requestUrl)
        check(png.size >= 8 && png[0] == 0x89.toByte() && png[1] == 0x50.toByte()) {
            "MRMS GetMap did not return a PNG (${png.size} bytes)"
        }
        return MrmsMapRequest(
            layerName = LAYER,
            capabilitiesUrl = CAPABILITIES_URL,
            urlTemplate = URL_TEMPLATE,
            requestUrl = requestUrl,
            time = time,
            timeCount = dimension.times.size,
            png = png,
        )
    }

    fun parseTimeDimension(xml: ByteArray): WmsTimeDimension {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(xml))
        val nodes = document.getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val node = nodes.item(index)
            if (node !is Element || node.localName != "Dimension") {
                continue
            }
            if (node.getAttribute("name") != "time") {
                continue
            }
            val times = node.textContent
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val defaultTime = node.getAttribute("default").ifBlank { null }
            return WmsTimeDimension(defaultTime, times)
        }
        error("GetCapabilities has no TIME dimension for $LAYER")
    }

    fun getMapUrl(time: String?, box: MercatorBox, width: Int, height: Int): String {
        val bbox = listOf(box.minX, box.minY, box.maxX, box.maxY).joinToString(",") { it.toString() }
        val builder = StringBuilder()
        builder.append(ENDPOINT)
        builder.append("?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap")
        builder.append("&LAYERS=").append(LAYER)
        builder.append("&STYLES=")
        builder.append("&FORMAT=image/png&TRANSPARENT=TRUE&CRS=EPSG:3857")
        builder.append("&BBOX=").append(bbox)
        builder.append("&WIDTH=").append(width)
        builder.append("&HEIGHT=").append(height)
        if (time != null) {
            builder.append("&TIME=").append(encodeQuery(time))
        }
        return builder.toString()
    }

    data class MercatorBox(
        val minX: Double,
        val minY: Double,
        val maxX: Double,
        val maxY: Double,
    ) {
        val aspect: Double = (maxX - minX) / (maxY - minY)
    }

    fun conusBox(): MercatorBox {
        return MercatorBox(
            minX = WebMercator.mercatorX(-126.0),
            minY = WebMercator.mercatorY(23.0),
            maxX = WebMercator.mercatorX(-66.0),
            maxY = WebMercator.mercatorY(50.0),
        )
    }

    private fun encodeQuery(value: String): String {
        return java.net.URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")
    }

    companion object {
        const val LAYER = "conus_cref_qcd"
        const val ENDPOINT = "https://opengeo.ncep.noaa.gov/geoserver/conus/conus_cref_qcd/ows"
        const val CAPABILITIES_URL =
            "$ENDPOINT?service=WMS&version=1.3.0&request=GetCapabilities"
        const val URL_TEMPLATE =
            "$ENDPOINT?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=$LAYER&STYLES=" +
                "&FORMAT=image/png&TRANSPARENT=TRUE&CRS=EPSG:3857" +
                "&BBOX={minx},{miny},{maxx},{maxy}&WIDTH={width}&HEIGHT={height}&TIME={iso8601}"
    }
}
