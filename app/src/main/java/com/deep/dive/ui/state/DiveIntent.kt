package com.deep.dive.ui.state

import com.deep.dive.domain.model.DiveSpot

enum class LocationPrecision {
    PRECISE, COARSE
}

sealed interface DiveIntent {
    data object Login : DiveIntent
    data object SheetClose : DiveIntent
    data class Select(val spot: DiveSpot) : DiveIntent

}