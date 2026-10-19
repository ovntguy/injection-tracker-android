package com.deeeelay.aaradar.spike

import java.time.Instant

data class OsmTile(val coord: TileCoord, val png: ByteArray)

data class LocalRadarLoad(
    val site: NexradSite,
    val product: DownloadedProduct,
    val frame: ReflectivityFrame,
    val tile: OsmTile?,
    val tileError: String?,
)

class RadarFrameLoader(
    private val sites: List<NexradSite> = NexradCatalog.load(),
    private val nexrad: NexradClient = NexradClient(),
    private val fetch: (String) -> ByteArray = ::httpGet,
) {
    fun loadNearest(latitude: Double, longitude: Double, now: Instant = Instant.now(), osmZoom: Int = 5): LocalRadarLoad {
        val site = NexradCatalog.nearest(sites, latitude, longitude)
        val product = nexrad.fetchLatestReflectivity(site, now)
        val frame = NexradLevel3.decode(product.bytes, product.url).let { decoded ->
            if (decoded.siteId.isNotEmpty()) decoded else replaceSite(decoded, site.bucketId)
        }
        val coord = WebMercator.tileContaining(frame.latitude, frame.longitude, osmZoom)
        val tileResult = runCatching { OsmTile(coord, fetch(coord.url)) }
        return LocalRadarLoad(
            site = site,
            product = product,
            frame = frame,
            tile = tileResult.getOrNull(),
            tileError = tileResult.exceptionOrNull()?.message,
        )
    }

    private fun replaceSite(frame: ReflectivityFrame, siteId: String): ReflectivityFrame {
        return ReflectivityFrame(
            siteId = siteId,
            productCode = frame.productCode,
            latitude = frame.latitude,
            longitude = frame.longitude,
            scanTime = frame.scanTime,
            gateKm = frame.gateKm,
            binCount = frame.binCount,
            radials = frame.radials,
            startTenthDeg = frame.startTenthDeg,
            deltaTenthDeg = frame.deltaTenthDeg,
            dbzMin = frame.dbzMin,
            dbzInc = frame.dbzInc,
            sourceUrl = frame.sourceUrl,
        )
    }
}
