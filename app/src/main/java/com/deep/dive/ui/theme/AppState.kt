package com.deep.dive.ui.theme

import com.mapbox.geojson.Point

sealed interface AppState {
    data object Uninitialized : AppState

    sealed interface Initialized : AppState {
        data object Unauthorized : Initialized
        sealed interface Authorized : Initialized {
            sealed interface MapView : Authorized {
                data object SheetClosed: MapView
                data class SheetOpened(val point: Point): MapView
            }
            data object Settings : Authorized
        }
    }
}