package com.deep.dive.ui.state

import com.deep.dive.domain.model.DiveSpot

enum class LocationPrecision {
    PRECISE, COARSE
}

sealed interface DiveIntent {
    data object Login : DiveIntent
    data object SheetClose : DiveIntent
    data class Select(val spot: DiveSpot) : DiveIntent

    data object RequestLocation : DiveIntent
    data class PermissionGranted(
        val precision: LocationPrecision
    ) : DiveIntent
    data class LocationSuccess(
        val userPoint: com.mapbox.geojson.Point,
        val precision: LocationPrecision
    ) : DiveIntent
    data object LocationDenied : DiveIntent

}