package com.deep.dive.ui

import com.mapbox.geojson.Point

sealed interface DiveIntent {
    data object Login: DiveIntent
    data object SheetClose: DiveIntent
    data class Select(val point: Point): DiveIntent

}