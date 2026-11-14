package com.deeeelay.injectiontracker.ui.silhouette

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class ZoneGeometryTest {
    // A 2.2 dp/unit figure at 2.625 px/dp (1080-wide phone).
    private val pxPerDp = 2.625f
    private val w = 140f * 2.2f * pxPerDp
    private val h = 220f * 2.2f * pxPerDp

    private fun centerPx(site: InjectionSite) = ZoneGeometry.markerCenter(site, w, h)

    @Test
    fun markerCentersDeriveFromZonesJsonCxCy() {
        val json = zonesJson()
        val zone = Regex(""""id": "([a-z_]+)",\s*"label": "[^"]*",\s*"cx": ([0-9.]+),\s*"cy": ([0-9.]+)""")
        val parsed = zone.findAll(json).associate { m ->
            m.groupValues[1] to (m.groupValues[2].toFloat() to m.groupValues[3].toFloat())
        }
        assertThat(parsed).hasSize(18)
        val vw = 140f
        val vh = 220f
        InjectionSite.entries.forEach { site ->
            val (cx, cy) = parsed.getValue(site.zoneId)
            val p = ZoneGeometry.markerCenter(site, w, h)
            assertThat(p.x).isWithin(1e-3f).of(cx / vw * w)
            assertThat(p.y).isWithin(1e-3f).of(cy / vh * h)
        }
    }

    @Test
    fun tapOnEveryCenterSnapsToThatZone() {
        InjectionSite.entries.forEach { site ->
            val c = centerPx(site)
            assertThat(ZoneGeometry.snapToZone(c.x, c.y, w, h, pxPerDp)).isEqualTo(site)
        }
    }

    @Test
    fun tapBetweenZonesSnapsToNearest() {
        val a = centerPx(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL)
        val b = centerPx(InjectionSite.RIGHT_UPPER_THIGH_MID)
        // 30% of the way from proximal to mid -> proximal; 70% -> mid.
        val near = ZoneGeometry.snapToZone(a.x, a.y + (b.y - a.y) * 0.3f, w, h, pxPerDp)
        val far = ZoneGeometry.snapToZone(a.x, a.y + (b.y - a.y) * 0.7f, w, h, pxPerDp)
        assertThat(near).isEqualTo(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL)
        assertThat(far).isEqualTo(InjectionSite.RIGHT_UPPER_THIGH_MID)
    }

    @Test
    fun tapWithin28dpIsAcceptedAndBeyondIsRejected() {
        // Right upper arm proximal is the outermost-left, top zone; move straight up (away from all others).
        val c = centerPx(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL)
        val inside = ZoneGeometry.snapToZone(c.x, c.y - 27.9f * pxPerDp, w, h, pxPerDp)
        val edge = ZoneGeometry.snapToZone(c.x, c.y - 28f * pxPerDp, w, h, pxPerDp)
        val outside = ZoneGeometry.snapToZone(c.x, c.y - 28.2f * pxPerDp, w, h, pxPerDp)
        assertThat(inside).isEqualTo(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL)
        assertThat(edge).isEqualTo(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL)
        assertThat(outside).isNull()
    }

    @Test
    fun farTapsAreIgnored() {
        assertThat(ZoneGeometry.snapToZone(0f, 0f, w, h, pxPerDp)).isNull()
        assertThat(ZoneGeometry.snapToZone(w / 2, h, w, h, pxPerDp)).isNull()
    }

    @Test
    fun rejectRadiusIsDensityIndependent() {
        val c = ZoneGeometry.markerCenter(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL, 280f, 440f)
        // Same 30 dp offset at 1x and 3x density -> rejected both times.
        assertThat(ZoneGeometry.snapToZone(c.x, c.y - 30f, 280f, 440f, 1f)).isNull()
        val c3 = ZoneGeometry.markerCenter(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL, 840f, 1320f)
        assertThat(ZoneGeometry.snapToZone(c3.x, c3.y - 90f, 840f, 1320f, 3f)).isNull()
        assertThat(ZoneGeometry.snapToZone(c3.x, c3.y - 81f, 840f, 1320f, 3f))
            .isEqualTo(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL)
    }

    @Test
    fun logFigureScaleStaysBetween70And75PercentOfWidthAndAbove1point6() {
        val width = 411f
        // Plenty of height: capped at 75% of width.
        assertThat(ZoneGeometry.logFigureScale(width, 2000f) * 140f).isWithin(0.01f).of(width * 0.75f)
        // Little height: floored at 70% of width (column scrolls).
        assertThat(ZoneGeometry.logFigureScale(width, 100f) * 140f).isWithin(0.01f).of(width * 0.70f)
        // In between: fills the height.
        assertThat(ZoneGeometry.logFigureScale(width, 460f)).isWithin(0.001f).of(460f / 220f)
        // Narrow screen: never below 1.6 dp per unit.
        assertThat(ZoneGeometry.logFigureScale(200f, 100f)).isAtLeast(1.6f)
    }

    @Test
    fun glyphStatesDifferAndSelectedWins() {
        val a = InjectionSite.LEFT_ABDOMEN_MID
        val b = InjectionSite.RIGHT_UPPER_ARM_MID
        assertThat(zoneGlyph(a, selected = null, lastSite = null)).isEqualTo(ZoneGlyph.Rest)
        assertThat(zoneGlyph(a, selected = null, lastSite = a)).isEqualTo(ZoneGlyph.LastSite)
        assertThat(zoneGlyph(a, selected = a, lastSite = null)).isEqualTo(ZoneGlyph.Selected)
        assertThat(zoneGlyph(a, selected = a, lastSite = a)).isEqualTo(ZoneGlyph.Selected)
        assertThat(zoneGlyph(b, selected = a, lastSite = a)).isEqualTo(ZoneGlyph.Rest)
    }

    @Test
    fun glyphSizesMatchDesignSpec() {
        assertThat(ZoneGeometry.REST_RING_DP).isEqualTo(14f)
        assertThat(ZoneGeometry.SELECTED_DISC_DP).isEqualTo(20f)
        assertThat(ZoneGeometry.SELECTED_CHECK_DP).isEqualTo(12f)
        assertThat(ZoneGeometry.LAST_SITE_DOT_DP).isEqualTo(6f)
        assertThat(ZoneGeometry.HOME_DOT_DP).isEqualTo(10f)
        assertThat(ZoneGeometry.REJECT_DISTANCE_DP).isEqualTo(28f)
    }

    private fun zonesJson(): String = listOf(
        File("app/src/main/assets/silhouette_zones.json"),
        File("../app/src/main/assets/silhouette_zones.json"),
        File("src/main/assets/silhouette_zones.json"),
    ).first { it.exists() }.readText()
}
