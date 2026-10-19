package com.deeeelay.aaradar.spike

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

/**
 * Minimal NEXRAD Level 3 decoder for digital reflectivity (packet code 16).
 *
 * Written from the public NEXRAD ICD message layout (18-byte header, 102-byte product
 * description, symbology block, digital radial packet). This is not wX source.
 * wX (GPL-3.0, joshuatee.wx.radar) folds decode into Android OpenGL buffers
 * (`NexradDecodeEightBit`, `NexradLevel3`, `OglRadarBuffers`) and was not extracted.
 *
 * Products exercised: 153 (N0B super-res, 0.25 km / 0.5°) and 94 (N0Q). Live files from
 * Unidata and tgftp are bzip2-compressed after the product description block. A packet
 * scale factor of 999 is the sentinel those products write; gate spacing then comes from
 * the ICD range (460 km) rather than treating 999 as 999 meters.
 */
class ReflectivityFrame(
    val siteId: String,
    val productCode: Int,
    val latitude: Double,
    val longitude: Double,
    val scanTime: Instant,
    val gateKm: Double,
    val binCount: Int,
    val radials: List<ByteArray>,
    val startTenthDeg: IntArray,
    val deltaTenthDeg: IntArray,
    val dbzMin: Double,
    val dbzInc: Double,
    val sourceUrl: String,
) {
    val productName: String = when (productCode) {
        153 -> "N0B"
        94 -> "N0Q"
        else -> "P$productCode"
    }
    val maxRangeKm: Double = binCount * gateKm
    private val azimuthLut: IntArray = buildAzimuthLut()

    fun dbz(raw: Int): Double? {
        if (raw < 2) {
            return null
        }
        return dbzMin + (raw - 2) * dbzInc
    }

    fun sampleRaw(azimuthDeg: Double, rangeKm: Double): Int {
        if (rangeKm < 0.0 || rangeKm >= maxRangeKm || binCount == 0) {
            return 0
        }
        val bin = (rangeKm / gateKm).toInt()
        if (bin !in 0 until binCount) {
            return 0
        }
        val tenth = tenthOf(azimuthDeg)
        val radial = azimuthLut[tenth]
        if (radial < 0) {
            return 0
        }
        val gates = radials[radial]
        if (bin >= gates.size) {
            return 0
        }
        return gates[bin].toInt() and 0xFF
    }

    private fun buildAzimuthLut(): IntArray {
        val lut = IntArray(3600) { -1 }
        for (index in radials.indices) {
            val start = Math.floorMod(startTenthDeg[index], 3600)
            val span = deltaTenthDeg[index].coerceAtLeast(1)
            for (step in 0 until span) {
                lut[(start + step) % 3600] = index
            }
        }
        return lut
    }

    private fun tenthOf(azimuthDeg: Double): Int {
        val wrapped = (azimuthDeg % 360.0 + 360.0) % 360.0
        val tenth = (wrapped * 10.0).toInt()
        return if (tenth >= 3600) 0 else tenth
    }
}

object NexradLevel3 {
    private const val HEADER_BYTES = 18
    private const val PDB_BYTES = 102
    private const val MAX_RANGE_KM = 460.0

    fun decode(bytes: ByteArray, sourceUrl: String = ""): ReflectivityFrame {
        val siteFromHeader = readAwipsSite(bytes)
        val message = trimMessage(stripWmo(bytes))
        val buffer = ByteBuffer.wrap(message).order(ByteOrder.BIG_ENDIAN)
        val productCode = buffer.short.toInt() and 0xFFFF
        val headerDate = buffer.short.toInt() and 0xFFFF
        val headerSeconds = buffer.int
        val messageLength = buffer.int
        buffer.short // source id
        buffer.short // destination id
        buffer.short // block count
        check(buffer.position() == HEADER_BYTES)

        val divider = buffer.short.toInt()
        check(divider == -1) { "Product description divider was $divider, expected -1" }
        val latitude = buffer.int / 1000.0
        val longitude = buffer.int / 1000.0
        buffer.short // height, feet
        val describedCode = buffer.short.toInt() and 0xFFFF
        buffer.short // operational mode
        buffer.short // vcp
        buffer.short // sequence
        buffer.short // volume number
        val volumeDate = buffer.short.toInt() and 0xFFFF
        val volumeSeconds = buffer.int
        buffer.short // generation date
        buffer.int // generation time
        val dep1 = buffer.short.toInt()
        val dep2 = buffer.short.toInt()
        buffer.short // elevation number
        val dep3 = buffer.short.toInt()
        val thresholds = IntArray(16) { buffer.short.toInt() }
        val depRest = IntArray(7) { buffer.short.toInt() }
        buffer.get() // version
        buffer.get() // spot blank
        val symbologyHalfwords = buffer.int
        buffer.int // graphic offset
        buffer.int // tabular offset
        check(buffer.position() == HEADER_BYTES + PDB_BYTES) {
            "Product description ended at ${buffer.position()}, expected ${HEADER_BYTES + PDB_BYTES}"
        }

        val dependencies = intArrayOf(dep1, dep2, dep3, depRest[0], depRest[1], depRest[2], depRest[3], depRest[4], depRest[5], depRest[6])
        val compressed = dependencies[7] != 0
        val payload = if (compressed) {
            val compressedBytes = message.copyOfRange(buffer.position(), message.size)
            val decoded = BZip2CompressorInputStream(compressedBytes.inputStream()).use { it.readBytes() }
            message.copyOfRange(0, HEADER_BYTES + PDB_BYTES) + decoded
        } else {
            if (messageLength in (HEADER_BYTES + PDB_BYTES)..message.size) {
                message.copyOf(messageLength)
            } else {
                message
            }
        }

        val block = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
        val symbology = symbologyHalfwords * 2
        check(symbology in 0 until payload.size - 10) { "Symbology offset $symbology is outside ${payload.size} bytes" }
        block.position(symbology)
        val symDivider = block.short.toInt()
        val blockId = block.short.toInt()
        block.int // block length
        val layers = block.short.toInt() and 0xFFFF
        check(symDivider == -1 && blockId == 1) { "Symbology header $symDivider / $blockId" }
        check(layers >= 1) { "No symbology layers" }
        val layerDivider = block.short.toInt()
        val layerLength = block.int
        check(layerDivider == -1) { "Layer divider $layerDivider" }
        val layerEnd = block.position() + layerLength
        val packet = block.short.toInt() and 0xFFFF
        check(packet == 16) { "Expected digital radial packet 16, found $packet" }

        val firstBin = block.short.toInt() and 0xFFFF
        val binCount = block.short.toInt() and 0xFFFF
        block.short // i center
        block.short // j center
        val scaleFactor = block.short.toInt() and 0xFFFF
        val radialCount = block.short.toInt() and 0xFFFF
        check(binCount > 0 && radialCount > 0) { "Empty radial packet" }

        val radials = ArrayList<ByteArray>(radialCount)
        val starts = IntArray(radialCount)
        val deltas = IntArray(radialCount)
        for (index in 0 until radialCount) {
            check(block.position() + 6 <= payload.size) { "Radial $index ran past the file" }
            val byteCount = block.short.toInt() and 0xFFFF
            starts[index] = block.short.toInt() and 0xFFFF
            deltas[index] = block.short.toInt() and 0xFFFF
            check(block.position() + byteCount <= payload.size) { "Radial $index data ran past the file" }
            val gates = ByteArray(byteCount)
            block.get(gates)
            radials += gates
        }
        check(block.position() <= layerEnd + 4) { "Radials spilled past the symbology layer" }

        val minDbz = thresholds[0] * 0.1
        val incDbz = thresholds[1] * 0.1
        val code = if (describedCode != 0) describedCode else productCode
        val scanDays = if (volumeDate != 0) volumeDate else headerDate
        val scanSeconds = if (volumeDate != 0) volumeSeconds else headerSeconds
        return ReflectivityFrame(
            siteId = siteFromHeader,
            productCode = code,
            latitude = latitude,
            longitude = longitude,
            scanTime = nexradInstant(scanDays, scanSeconds),
            gateKm = gateSpacingKm(code, scaleFactor, binCount),
            binCount = binCount,
            radials = radials,
            startTenthDeg = starts,
            deltaTenthDeg = deltas,
            dbzMin = minDbz,
            dbzInc = incDbz,
            sourceUrl = sourceUrl,
        ).also {
            check(firstBin == 0 || firstBin < binCount) { "Unexpected first-bin index $firstBin" }
        }
    }

    fun gateSpacingKm(productCode: Int, scaleFactor: Int, binCount: Int): Double {
        if (scaleFactor == 999) {
            return if (productCode == 153) 0.25 else MAX_RANGE_KM / binCount
        }
        if (scaleFactor <= 0) {
            return MAX_RANGE_KM / binCount
        }
        return scaleFactor / 1000.0
    }

    fun nexradInstant(daysSince1970Inclusive: Int, secondsAfterMidnight: Int): Instant {
        return Instant.EPOCH
            .plus((daysSince1970Inclusive - 1).toLong(), ChronoUnit.DAYS)
            .plusSeconds(secondsAfterMidnight.toLong())
    }

    private fun readAwipsSite(bytes: ByteArray): String {
        val head = bytes.copyOfRange(0, minOf(bytes.size, 48)).toString(Charsets.US_ASCII)
        val match = Regex("""N0[A-Z]([A-Z]{3})""").find(head)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun stripWmo(bytes: ByteArray): ByteArray {
        val head = bytes.copyOfRange(0, minOf(bytes.size, 8)).toString(Charsets.US_ASCII)
        if (!head.startsWith("SDUS") && !head.startsWith("NXUS") && !head.startsWith("NOUS")) {
            return bytes
        }
        var found = 0
        var index = 0
        while (index + 2 < bytes.size && found < 2) {
            if (bytes[index] == 13.toByte() && bytes[index + 1] == 13.toByte() && bytes[index + 2] == 10.toByte()) {
                found += 1
                index += 3
                if (found == 2) {
                    return bytes.copyOfRange(index, bytes.size)
                }
            } else {
                index += 1
            }
        }
        return bytes
    }

    private fun trimMessage(bytes: ByteArray): ByteArray {
        if (bytes.size >= 4 && bytes.copyOfRange(bytes.size - 4, bytes.size - 1).contentEquals(byteArrayOf(13, 13, 10))) {
            return bytes.copyOf(bytes.size - 4)
        }
        return bytes
    }
}
