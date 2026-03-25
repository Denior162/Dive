package com.deep.dive.ui.theme

sealed interface AppState {
    data object Uninitialized : AppState

    sealed interface Initialized : AppState {
        data object Unauthorized : Initialized
        sealed interface Authorized : Initialized {
            data object MapView : Authorized
            data object Settings : Authorized
        }
    }
}