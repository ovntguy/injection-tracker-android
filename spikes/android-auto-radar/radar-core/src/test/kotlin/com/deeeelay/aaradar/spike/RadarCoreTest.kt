package com.deeeelay.aaradar.spike

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NexradLevel3Test {
    @Test
    fun decodesUncompressedDigitalRadialPacket() {
        val frame = NexradLevel3.decode(syntheticN0q())
        assertEquals(94, frame.productCode)
        assertEquals("N0Q", frame.productName)
        assertEquals(33.172, frame.latitude, 0.0001)
        assertEquals(-86.770, frame.longitude, 0.0001)
        assertEquals(Instant.parse("1970-01-02T01:01:01Z"), frame.scanTime)
        assertEquals(1.0, frame.gateKm, 0.0001)
        assertEquals(4, frame.binCount)
        assertEquals(1, frame.radials.size)
        assertEquals(null, frame.dbz(0))
        assertEquals(null, frame.dbz(1))
        assertEquals(-32.0, frame.dbz(2)!!, 0.001)
        assertEquals(18.0, frame.dbz(102)!!, 0.001)
        assertEquals(102, frame.sampleRaw(azimuthDeg = 10.0, rangeKm = 3.2))
    }

    @Test
    fun sentinelScaleUsesIcdGateSpacing() {
        assertEquals(0.25, NexradLevel3.gateSpacingKm(153, 999, 1840), 0.0001)
        assertEquals(1.0, NexradLevel3.gateSpacingKm(94, 999, 460), 0.0001)
        assertEquals(0.25, NexradLevel3.gateSpacingKm(94, 250, 460), 0.0001)
    }

    private fun syntheticN0q(): ByteArray {
        val gates = byteArrayOf(0, 1, 2, 102)
        val packetAndRadials = 2 + 12 + 6 + gates.size
        val symbologyBytes = 10 + 6 + packetAndRadials
        val message = ByteBuffer.allocate(18 + 102 + symbologyBytes).order(ByteOrder.BIG_ENDIAN)
        message.putShort(94)
        message.putShort(2)
        message.putInt(3661)
        message.putInt(18 + 102 + symbologyBytes)
        message.putShort(0)
        message.putShort(0)
        message.putShort(3)
        message.putShort(-1)
        message.putInt(33172)
        message.putInt(-86770)
        message.putShort(5000)
        message.putShort(94)
        message.putShort(1)
        message.putShort(35)
        message.putShort(1)
        message.putShort(1)
        message.putShort(2)
        message.putInt(3661)
        message.putShort(2)
        message.putInt(3661)
        message.putShort(0)
        message.putShort(0)
        message.putShort(1)
        message.putShort(0)
        message.putShort(-320)
        message.putShort(5)
        message.putShort(254)
        repeat(13) { message.putShort(0) }
        repeat(7) { message.putShort(0) }
        message.put(0)
        message.put(0)
        message.putInt(60)
        message.putInt(0)
        message.putInt(0)
        assertEquals(120, message.position())
        message.putShort(-1)
        message.putShort(1)
        message.putInt(symbologyBytes)
        message.putShort(1)
        message.putShort(-1)
        message.putInt(packetAndRadials)
        message.putShort(16)
        message.putShort(0)
        message.putShort(gates.size.toShort())
        message.putShort(0)
        message.putShort(0)
        message.putShort(1000)
        message.putShort(1)
        message.putShort(gates.size.toShort())
        message.putShort(0)
        message.putShort(3600)
        message.put(gates)
        return message.array()
    }
}

class CatalogAndWmsTest {
    @Test
    fun nearestWsr88dForBirminghamAndHouston() {
        val sites = NexradCatalog.load()
        assertTrue(sites.size > 100)
        assertEquals("KBMX", NexradCatalog.nearest(sites, DEFAULT_LATITUDE, DEFAULT_LONGITUDE).id)
        assertEquals("KHGX", NexradCatalog.nearest(sites, 29.7604, -95.3698).id)
    }

    @Test
    fun latestS3KeyIsLexicographicMax() {
        val xml = """
            <ListBucketResult>
              <Contents><Key>BMX_N0B_2026_10_19_16_33_39</Key></Contents>
              <Contents><Key>BMX_N0B_2026_10_19_16_40_42</Key></Contents>
            </ListBucketResult>
        """.trimIndent()
        assertEquals("BMX_N0B_2026_10_19_16_40_42", NexradClient().latestKey(xml))
    }

    @Test
    fun countsExplicitWmsTimes() {
        val xml = """
            <WMS>
              <Dimension name="time" default="2026-10-19T16:42:36Z">
                2026-10-19T16:40:40.000Z,2026-10-19T16:42:36.000Z
              </Dimension>
            </WMS>
        """.trimIndent().toByteArray()
        val dimension = MrmsClient().parseTimeDimension(xml)
        assertEquals(2, dimension.times.size)
        assertEquals("2026-10-19T16:42:36Z", dimension.defaultTime)
        assertTrue(MrmsClient.URL_TEMPLATE.contains("LAYERS=conus_cref_qcd"))
        assertTrue(MrmsClient.URL_TEMPLATE.contains("TIME={iso8601}"))
    }
}

class RadarCompositorTest {
    @Test
    fun paintsEchoAtRadarCenter() {
        val frame = ReflectivityFrame(
            siteId = "BMX",
            productCode = 153,
            latitude = 33.172,
            longitude = -86.770,
            scanTime = Instant.EPOCH,
            gateKm = 20.0,
            binCount = 4,
            radials = listOf(byteArrayOf(180.toByte(), 0, 0, 0)),
            startTenthDeg = intArrayOf(0),
            deltaTenthDeg = intArrayOf(3600),
            dbzMin = -32.0,
            dbzInc = 0.5,
            sourceUrl = "synthetic",
        )
        val image = RadarCompositor.compose(frame, width = 64, height = 64, dark = true)
        val center = image.pixel(32, 32)
        assertTrue(center != 0xFF101418.toInt(), "center pixel should show reflectivity")
    }
}
