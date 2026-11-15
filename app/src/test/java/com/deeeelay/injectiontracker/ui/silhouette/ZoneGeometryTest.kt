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
    fun logFigureScalesByBodyCropBetween1point7And2point0() {
        val cropH = ZoneGeometry.LOG_BODY_CROP.height
        // Plenty of height: capped at the 2.0 dp/unit target (no width cap).
        assertThat(ZoneGeometry.logFigureScale(2000f)).isEqualTo(2.0f)
        // Tight: floored at 1.7 dp/unit; the screen scrolls instead.
        assertThat(ZoneGeometry.logFigureScale(100f)).isEqualTo(1.7f)
        // In between: the body crop fills the available height.
        assertThat(ZoneGeometry.logFigureScale(cropH * 1.85f)).isWithin(1e-4f).of(1.85f)
    }

    @Test
    fun bodyCropIsDesignBodyBoundsPlusSixUnits() {
        // Design polish spec (revised 09:45 CST): bodyBounds x 32.9–107.1, y 5–215, + 6 units.
        assertThat(ZoneGeometry.BODY_X).isEqualTo(32.9f)
        assertThat(ZoneGeometry.BODY_X + ZoneGeometry.BODY_WIDTH).isWithin(1e-4f).of(107.1f)
        assertThat(ZoneGeometry.BODY_Y).isEqualTo(5f)
        assertThat(ZoneGeometry.BODY_Y + ZoneGeometry.BODY_HEIGHT).isEqualTo(215f)
        val crop = ZoneGeometry.LOG_BODY_CROP
        assertThat(crop.x).isWithin(1e-4f).of(26.9f)
        assertThat(crop.y).isEqualTo(-1f)
        assertThat(crop.width).isWithin(1e-4f).of(86.2f)
        assertThat(crop.height).isEqualTo(222f)
        // The crop is centered on the body, and every zone center is inside it.
        assertThat(crop.x + crop.width / 2).isWithin(1e-4f).of(ZoneGeometry.BODY_X + ZoneGeometry.BODY_WIDTH / 2)
        InjectionSite.entries.forEach { site ->
            assertThat(site.viewportX).isIn(com.google.common.collect.Range.open(crop.x, crop.x + crop.width))
            assertThat(site.viewportY).isIn(com.google.common.collect.Range.open(crop.y, crop.y + crop.height))
        }
    }

    @Test
    fun croppedMarkerCentersStillDeriveFromCxCyAndSnap() {
        val crop = ZoneGeometry.LOG_BODY_CROP
        val unitPx = 2.0f * pxPerDp
        val cw = crop.width * unitPx
        val ch = crop.height * unitPx
        InjectionSite.entries.forEach { site ->
            val p = ZoneGeometry.markerCenter(site, cw, ch, crop)
            assertThat(p.x).isWithin(1e-2f).of((site.viewportX - crop.x) * unitPx)
            assertThat(p.y).isWithin(1e-2f).of((site.viewportY - crop.y) * unitPx)
            assertThat(ZoneGeometry.snapToZone(p.x, p.y, cw, ch, pxPerDp, crop)).isEqualTo(site)
        }
        val top = ZoneGeometry.markerCenter(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL, cw, ch, crop)
        assertThat(ZoneGeometry.snapToZone(top.x, top.y - 28.2f * pxPerDp, cw, ch, pxPerDp, crop)).isNull()
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
        assertThat(ZoneGeometry.HOME_HALO_DP).isEqualTo(1.5f)
        assertThat(ZoneGeometry.HOME_RING_STROKE_DP).isEqualTo(1.5f)
        assertThat(ZoneGeometry.HOME_MARKER_DP).isEqualTo(16f)
        assertThat(ZoneGeometry.REJECT_DISTANCE_DP).isEqualTo(28f)
    }

    private fun zonesJson(): String = listOf(
        File("app/src/main/assets/silhouette_zones.json"),
        File("../app/src/main/assets/silhouette_zones.json"),
        File("src/main/assets/silhouette_zones.json"),
    ).first { it.exists() }.readText()
}
