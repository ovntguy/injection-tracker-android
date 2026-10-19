package com.deeeelay.aaradar.spike

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI

/**
 * Identifies this spike on NOAA and OpenStreetMap requests.
 * OSM's tile policy requires a unique, non-browser user agent and forbids bulk scraping.
 * This client fetches a single tile per frame.
 */
const val SPIKE_USER_AGENT =
    "BigDawgAaRadarSpike/0.1 (personal sideload feasibility; single OSM tile)"

/** Birmingham, Alabama. Demo hard-code, not live GPS. Nearest WSR-88D is KBMX (Alabaster). */
const val DEFAULT_LATITUDE = 33.5186
const val DEFAULT_LONGITUDE = -86.8104

fun httpGet(url: String): ByteArray {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 20_000
    connection.readTimeout = 60_000
    connection.setRequestProperty("User-Agent", SPIKE_USER_AGENT)
    connection.setRequestProperty("Accept", "*/*")
    connection.instanceFollowRedirects = true
    try {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.use { input ->
            val buffer = ByteArrayOutputStream()
            input.copyTo(buffer)
            buffer.toByteArray()
        } ?: ByteArray(0)
        if (code !in 200..299) {
            val snippet = body.toString(Charsets.UTF_8).take(180)
            error("HTTP $code for $url $snippet")
        }
        return body
    } finally {
        connection.disconnect()
    }
}
