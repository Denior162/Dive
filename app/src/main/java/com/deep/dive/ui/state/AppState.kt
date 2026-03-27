package com.deep.dive.ui.state

import com.deep.dive.domain.model.DiveSpot
import com.mapbox.geojson.Point

sealed interface AppState {
    data object Uninitialized : AppState

    sealed interface Initialized : AppState {
        data object Unauthorized : Initialized
        sealed interface Authorized : Initialized {
            val navScreen: NavScreen
            data class WithLocation(
                val userPoint: Point,
                val precision: LocationPrecision,
                override val navScreen: NavScreen
            ) : Authorized
            data class WithoutLocation(
                val reason: LocationReason,
                override val navScreen: NavScreen
            ) : Authorized
        }
    }
}

sealed interface LocationReason {
    data object Pending : LocationReason
    data object Denied : LocationReason
}


sealed interface NavScreen {
    sealed interface MapView : NavScreen {
        data object SheetClosed : MapView
        data class SheetOpened(val spot: DiveSpot) : MapView
    }

    data object Settings : NavScreen
}