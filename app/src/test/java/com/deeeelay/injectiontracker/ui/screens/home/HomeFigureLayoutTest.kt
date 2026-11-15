package com.deeeelay.injectiontracker.ui.screens.home

import com.deeeelay.injectiontracker.ui.silhouette.ZoneGeometry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Home figure height cap (BUILD FROM HERE, rev 2026-11-15). */
class HomeFigureLayoutTest {
    @Test
    fun maxCapIs1_6DpPerBodyUnit() {
        assertThat(HomeFigureLayout.MAX_DP).isEqualTo(1.6f * 210f)
        assertThat(HomeFigureLayout.MAX_DP).isEqualTo(336f)
        assertThat(ZoneGeometry.BODY_HEIGHT).isEqualTo(210f)
    }

    @Test
    fun leftoverIsClampedTo88And336() {
        assertThat(HomeFigureLayout.figureHeight(500f)).isEqualTo(336f)
        assertThat(HomeFigureLayout.figureHeight(336f)).isEqualTo(336f)
        assertThat(HomeFigureLayout.figureHeight(200f)).isEqualTo(200f)
        assertThat(HomeFigureLayout.figureHeight(90f)).isEqualTo(90f)
        assertThat(HomeFigureLayout.figureHeight(88f)).isEqualTo(88f)
        assertThat(HomeFigureLayout.figureHeight(87f)).isEqualTo(88f)
        assertThat(HomeFigureLayout.figureHeight(0f)).isEqualTo(88f)
    }

    @Test
    fun scrollsOnlyWhenLeftoverIsUnder88() {
        assertThat(HomeFigureLayout.scrolls(88f)).isFalse()
        assertThat(HomeFigureLayout.scrolls(200f)).isFalse()
        assertThat(HomeFigureLayout.scrolls(87.9f)).isTrue()
        assertThat(HomeFigureLayout.scrolls(0f)).isTrue()
    }

    @Test
    fun compactThresholdHidesSideLabelsAndUses12dpMarker() {
        assertThat(HomeFigureLayout.compact(160f)).isFalse()
        assertThat(HomeFigureLayout.compact(159.9f)).isTrue()
        assertThat(HomeFigureLayout.compact(90f)).isTrue()
        assertThat(HomeFigureLayout.markerDiameterDp(160f)).isEqualTo(ZoneGeometry.HOME_MARKER_DP)
        assertThat(HomeFigureLayout.markerDiameterDp(159f)).isEqualTo(12f)
        assertThat(HomeFigureLayout.markerDiameterDp(88f)).isEqualTo(12f)
    }
}
