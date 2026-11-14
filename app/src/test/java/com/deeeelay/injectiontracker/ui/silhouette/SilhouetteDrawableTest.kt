package com.deeeelay.injectiontracker.ui.silhouette

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class SilhouetteDrawableTest {
    @Test
    fun displayEmptyAndLastSiteMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, null))
            .isEqualTo("silhouette_display_empty")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.RIGHT_ABDOMEN_MID))
            .isEqualTo("silhouette_display_right_abdomen_mid")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.LEFT_UPPER_ARM_DISTAL))
            .isEqualTo("silhouette_display_left_upper_arm_distal")
    }

    @Test
    fun selectUnselectedAndZoneMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, null))
            .isEqualTo("silhouette_select_unselected")
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL))
            .isEqualTo("silhouette_select_right_upper_thigh_proximal")
    }

    @Test
    fun everyZoneHasDisplayAndSelectDrawableNames() {
        assertThat(InjectionSite.entries).hasSize(18)
        InjectionSite.entries.forEach { site ->
            val zone = site.zoneId
            assertThat(silhouetteDrawableName(SilhouetteMode.Display, site))
                .isEqualTo("silhouette_display_$zone")
            assertThat(silhouetteDrawableName(SilhouetteMode.Select, site))
                .isEqualTo("silhouette_select_$zone")
        }
    }

    @Test
    fun lockedVectorDrawablesExistForEmptyAndEveryZone() {
        val dir = drawableDir()
        val names = buildList {
            add("silhouette_display_empty.xml")
            add("silhouette_select_unselected.xml")
            InjectionSite.entries.forEach { site ->
                add("silhouette_display_${site.zoneId}.xml")
                add("silhouette_select_${site.zoneId}.xml")
            }
        }
        assertThat(names).hasSize(38)
        names.forEach { name ->
            assertThat(File(dir, name).exists()).isTrue()
        }
        val emptyPath = Regex("""android:pathData="([^"]+)"""").find(
            File(dir, "silhouette_display_empty.xml").readText(),
        )!!.groupValues[1]
        names.forEach { name ->
            val text = File(dir, name).readText()
            assertThat(text).contains(emptyPath)
            // One outline style for Home and Log (items 7/19): #79747E, 2.0 units.
            assertThat(text).contains("android:strokeWidth=\"2.0\"")
            assertThat(text).contains("android:strokeColor=\"#79747E\"")
            assertThat(text).doesNotContain("#F3EDF7")
            if (name != "silhouette_display_empty.xml" && name != "silhouette_select_unselected.xml") {
                assertThat(text).contains("a 2.0,2.0")
            }
        }
    }

    @Test
    fun anatomicalRightDrawablesKeepScreenLeftCenters() {
        val dir = drawableDir()
        val rightMid = File(dir, "silhouette_display_right_abdomen_mid.xml").readText()
        val leftMid = File(dir, "silhouette_display_left_abdomen_mid.xml").readText()
        assertThat(rightMid).contains("57.5,105.0")
        assertThat(leftMid).contains("78.5,105.0")
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportX).isEqualTo(59.5f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportX).isEqualTo(80.5f)
    }

    @Test
    fun zoneDotsUseLockedV2Centers() {
        val dir = drawableDir()
        InjectionSite.entries.forEach { site ->
            val display = File(dir, "silhouette_display_${site.zoneId}.xml").readText()
            val select = File(dir, "silhouette_select_${site.zoneId}.xml").readText()
            // Visual 2026-11-14 art: solid r=2.0 dots at the locked centers (legacy files, unwired).
            val displayStartX = site.viewportX - ART_DOT_RADIUS
            val selectStartX = site.viewportX - ART_DOT_RADIUS
            assertThat(display).contains("${fmt(displayStartX)},${fmt(site.viewportY)}")
            assertThat(select).contains("${fmt(selectStartX)},${fmt(site.viewportY)}")
        }
    }

    @Test
    fun nightVariantsMatchDayGeometry() {
        val day = drawableDir()
        val night = File(day.parentFile, "drawable-night")
        val dayFiles = day.listFiles { f -> f.name.startsWith("silhouette_") }!!.map { it.name }.sorted()
        val nightFiles = night.listFiles { f -> f.name.startsWith("silhouette_") }!!.map { it.name }.sorted()
        assertThat(nightFiles).containsExactlyElementsIn(dayFiles)
        val pathData = Regex("""android:pathData="([^"]+)"""")
        dayFiles.forEach { name ->
            val d = pathData.findAll(File(day, name).readText()).map { it.groupValues[1] }.toList()
            val n = pathData.findAll(File(night, name).readText()).map { it.groupValues[1] }.toList()
            assertThat(n).containsExactlyElementsIn(d).inOrder()
        }
    }

    @Test
    fun baseArtHasRightMarkerOnScreenLeftAndLeftMarkerOnScreenRight() {
        listOf("silhouette_display_empty.xml", "silhouette_select_unselected.xml").forEach { name ->
            val text = File(drawableDir(), name).readText()
            val r = Regex("""android:name="marker_R"\s+android:pathData="M([0-9.]+)""").find(text)!!
            val l = Regex("""android:name="marker_L"\s+android:pathData="M([0-9.]+)""").find(text)!!
            assertThat(r.groupValues[1].toFloat()).isLessThan(InjectionSite.VIEWPORT_WIDTH / 2)
            assertThat(l.groupValues[1].toFloat()).isGreaterThan(InjectionSite.VIEWPORT_WIDTH / 2)
        }
    }

    @Test
    fun nomarkersVariantsAreBaseArtWithoutRL() {
        listOf("drawable", "drawable-night").forEach { dirName ->
            val dir = File(drawableDir().parentFile, dirName)
            listOf(
                "silhouette_display_empty" to "silhouette_display_empty_nomarkers",
                "silhouette_select_unselected" to "silhouette_select_unselected_nomarkers",
            ).forEach { (base, small) ->
                val baseText = File(dir, "$base.xml").readText()
                val smallText = File(dir, "$small.xml").readText()
                assertThat(baseText).contains("marker_R")
                assertThat(smallText).doesNotContain("marker_R")
                assertThat(smallText).doesNotContain("marker_L")
                assertThat(smallText).doesNotContain("dot_")
                val body = Regex("""android:name="body_outline"\s+android:pathData="([^"]+)"""")
                assertThat(body.find(smallText)!!.groupValues[1]).isEqualTo(body.find(baseText)!!.groupValues[1])
            }
        }
    }

    @Test
    fun codeDrawnSideLabelsSitOnTheBakedGlyphs() {
        val text = File(drawableDir(), "silhouette_display_empty.xml").readText()
        fun bbox(name: String): List<Float> {
            val path = Regex("""android:name="$name"\s+android:pathData="([^"]+)"""").find(text)!!.groupValues[1]
            // Absolute M/H/V/L/Q coordinates; good enough for a bounding box of these glyphs.
            val nums = Regex("""-?[0-9]+(?:\.[0-9]+)?""").findAll(path).map { it.value.toFloat() }.toList()
            val xs = nums.filter { it > 10f && it < 130f && (if (name == "marker_R") it < 40f else it > 100f) }
            return listOf(xs.min(), xs.max())
        }
        val r = bbox("marker_R")
        val l = bbox("marker_L")
        assertThat(SideLabels.RIGHT_X).isIn(com.google.common.collect.Range.closed(r[0], r[1]))
        assertThat(SideLabels.LEFT_X).isIn(com.google.common.collect.Range.closed(l[0], l[1]))
        assertThat(SideLabels.RIGHT_X).isLessThan(InjectionSite.VIEWPORT_WIDTH / 2) // screen-left = anatomical right
        assertThat(SideLabels.Y).isIn(com.google.common.collect.Range.closed(18f, 30f))
        assertThat(SideLabels.TEXT_SP).isAtLeast(10f)
    }

    @Test
    fun sideLabelsOnlyWhenTheyFit() {
        assertThat(SideLabels.fits(178f)).isTrue() // Home with a last site (280 dp tall)
        assertThat(SideLabels.fits(140f)).isTrue() // Home empty state (220 dp tall)
        assertThat(SideLabels.fits(80f)).isFalse()
    }

    @Test
    fun homeUsesNomarkersArtAndLogKeepsBakedRL() {
        assertThat(silhouetteBaseDrawableRes(SilhouetteMode.Display))
            .isEqualTo(com.deeeelay.injectiontracker.R.drawable.silhouette_display_empty_nomarkers)
        assertThat(silhouetteBaseDrawableRes(SilhouetteMode.Select))
            .isEqualTo(com.deeeelay.injectiontracker.R.drawable.silhouette_select_unselected)
    }

    private companion object {
        const val ART_DOT_RADIUS = 2.0f
    }

    private fun fmt(value: Float): String {
        val tenths = value * 10f
        return if (tenths == tenths.toInt().toFloat()) {
            "%.1f".format(value)
        } else {
            "%.2f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    private fun drawableDir(): File {
        val candidates = listOf(
            File("app/src/main/res/drawable"),
            File("../app/src/main/res/drawable"),
            File("src/main/res/drawable"),
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("drawable dir not found from ${File(".").absolutePath}")
    }
}
