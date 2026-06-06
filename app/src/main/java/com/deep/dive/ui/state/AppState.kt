package com.deep.dive.ui.state

import com.deep.dive.domain.model.DiveSpot
import com.deep.dive.domain.model.UserProfileDto
import com.mapbox.geojson.Point

sealed interface AppState {
    data object Uninitialized : AppState

    sealed interface Initialized : AppState {
        data class Unauthorized(
            val status: LoginStatus = LoginStatus.Idle
        ) : Initialized

        sealed interface Authorized : Initialized {
            val user: UserProfileDto
            val diveSpots: List<DiveSpot>
            val navScreen: NavScreen

            data class WithLocation(
                override val user: UserProfileDto,
                val userPoint: Point,
                val precision: LocationPrecision,
                override val diveSpots: List<DiveSpot>,
                override val navScreen: NavScreen
            ) : Authorized

            data class WithoutLocation(
                override val user: UserProfileDto,
                val reason: LocationReason,
                override val diveSpots: List<DiveSpot>,
                override val navScreen: NavScreen
            ) : Authorized
        }
    }
}

sealed interface LocationReason {
    data object Pending : LocationReason
    data object Denied : LocationReason
}

sealed interface MapFocus {
    data class User(val point: Point) : MapFocus
    data class Spot(val diveSpot: DiveSpot) : MapFocus
    data object Manual : MapFocus
}

sealed interface NavScreen {
    sealed interface MapView : NavScreen {
        val focus: MapFocus

        data class SheetClosed(override val focus: MapFocus) : MapView


        data class SheetOpened(val spot: DiveSpot) : MapView {
            override val focus: MapFocus = MapFocus.Spot(spot)
        }
    }

    data object Settings : NavScreen
}

sealed interface LoginStatus {
    data object Idle : LoginStatus

    data class InvalidFormat(val emailError: String? = null, val passwordError: String? = null) :
        LoginStatus

    data object ReadyToSubmit : LoginStatus

    data object Loading : LoginStatus

    data class ServerError(val message: String) : LoginStatus}
