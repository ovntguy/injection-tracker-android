package com.deeeelay.aaradar.spike

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class NexradSite(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
) {
    val bucketId: String = id.takeLast(3)
}

object NexradCatalog {
    private val sitePattern = Regex(
        """\{"id":"([A-Z0-9]+)","lat":(-?[0-9.]+),"lon":(-?[0-9.]+),"name":"([^"]*)"\}""",
    )

    fun load(): List<NexradSite> {
        val stream = NexradCatalog::class.java.getResourceAsStream("/nexrad-sites.json")
            ?: error("nexrad-sites.json is not on the classpath")
        val json = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        return parse(json).filter { isWsr88d(it.id) }
    }

    fun parse(json: String): List<NexradSite> {
        return sitePattern.findAll(json).map { match ->
            NexradSite(
                id = match.groupValues[1],
                latitude = match.groupValues[2].toDouble(),
                longitude = match.groupValues[3].toDouble(),
                name = match.groupValues[4],
            )
        }.toList()
    }

    /** WSR-88D ICAO ids. TDWR sites in the same NWS feed start with T and are not N0B sources. */
    fun isWsr88d(id: String): Boolean {
        return id.startsWith("K") || id.startsWith("P") || id == "TJUA"
    }

    fun nearest(sites: List<NexradSite>, latitude: Double, longitude: Double): NexradSite {
        return sites.minBy { site ->
            haversineKm(latitude, longitude, site.latitude, site.longitude)
        }
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earth = 6371.0088
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(p1) * cos(p2) * sin(dLon / 2).pow(2)
        return 2 * earth * asin(sqrt(a))
    }
}

data class DownloadedProduct(
    val product: String,
    val url: String,
    val bytes: ByteArray,
)

class NexradClient(
    private val fetch: (String) -> ByteArray = ::httpGet,
) {
    fun fetchLatestReflectivity(site: NexradSite, now: Instant = Instant.now()): DownloadedProduct {
        val n0b = latestAws(site, "N0B", now)
        if (n0b != null) {
            return n0b
        }
        val url = tgftpN0q(site)
        return DownloadedProduct(product = "N0Q", url = url, bytes = fetch(url))
    }

    fun latestAws(site: NexradSite, product: String, now: Instant): DownloadedProduct? {
        val zone = now.atZone(ZoneOffset.UTC)
        for (hoursBack in 0..5) {
            val stamp = zone.minusHours(hoursBack.toLong())
            val prefix = "%s_%s_%s".format(
                site.bucketId,
                product,
                HOUR.format(stamp),
            )
            val listUrl = "$BUCKET/?list-type=2&prefix=$prefix&max-keys=40"
            val xml = fetch(listUrl).toString(Charsets.UTF_8)
            val key = latestKey(xml) ?: continue
            val objectUrl = "$BUCKET/$key"
            return DownloadedProduct(product = product, url = objectUrl, bytes = fetch(objectUrl))
        }
        return null
    }

    fun latestKey(listXml: String): String? {
        return KEY.findAll(listXml).map { it.groupValues[1] }.maxOrNull()
    }

    fun tgftpN0q(site: NexradSite): String {
        return "https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.${site.id.lowercase()}/sn.last"
    }

    private companion object {
        const val BUCKET = "https://unidata-nexrad-level3.s3.amazonaws.com"
        val HOUR: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH")
        val KEY: Regex = Regex("<Key>([^<]+)</Key>")
    }
}
