package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/** HARD LOCK: zones.json must stay byte-identical to main @ 3a72ffc. */
class ZonesJsonLockTest {
    @Test
    fun zonesJsonIsByteIdenticalToMain() {
        val bytes = listOf(
            File("app/src/main/assets/silhouette_zones.json"),
            File("../app/src/main/assets/silhouette_zones.json"),
            File("src/main/assets/silhouette_zones.json"),
        ).first { it.exists() }.readBytes()
        val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        assertThat(sha).isEqualTo(MAIN_SHA256)
    }

    private companion object {
        const val MAIN_SHA256 = "99eba4062bc6ca74e00ab372e06932d4a01a3176c2194d433fa6d4dd99d70496"
    }
}
