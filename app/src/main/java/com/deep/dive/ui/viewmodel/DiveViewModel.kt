package com.deep.dive.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.deep.dive.AppContainer
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.AppState.Initialized.Authorized.WithLocation
import com.deep.dive.ui.state.AppState.Initialized.Authorized.WithoutLocation
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.LocationPrecision
import com.deep.dive.ui.state.LocationReason
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.state.NavScreen.MapView.SheetOpened
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.geojson.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiveViewModel(
    private val locationClient: FusedLocationProviderClient
) : ViewModel() {
    private val _state = MutableStateFlow<AppState>(AppState.Uninitialized)

    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _state.value = AppState.Initialized.Unauthorized
        }
    }

    fun onIntent(intent: DiveIntent) {
        if (intent is DiveIntent.PermissionGranted) {
            fetchRealLocation(intent.precision)
            return
        }

        _state.update { currentState ->
            reduce(currentState, intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchRealLocation(precision: LocationPrecision) {
        locationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val realPoint = Point.fromLngLat(location.longitude, location.latitude)
                onIntent(DiveIntent.LocationSuccess(realPoint, precision))
            } ?: onIntent(DiveIntent.LocationDenied)
        }
    }

    private fun reduce(
        state: AppState,
        intent: DiveIntent
    ): AppState {
        if (state !is AppState.Initialized.Authorized) {
            return if (intent is DiveIntent.Login) {
                WithoutLocation(
                    reason = LocationReason.Pending,
                    navScreen = NavScreen.MapView.SheetClosed
                )
            } else state
        }

        return when (intent) {
            is DiveIntent.PermissionGranted -> state

            is DiveIntent.SheetClose -> state.updateNav(NavScreen.MapView.SheetClosed)
            is DiveIntent.Select -> state.updateNav(SheetOpened(intent.spot))

            is DiveIntent.RequestLocation -> WithoutLocation(
                reason = LocationReason.Pending,
                navScreen = state.navScreen
            )

            is DiveIntent.LocationSuccess -> WithLocation(
                userPoint = intent.userPoint,
                precision = intent.precision,
                navScreen = state.navScreen
            )

            is DiveIntent.LocationDenied -> WithoutLocation(
                reason = LocationReason.Denied,
                navScreen = state.navScreen
            )

            is DiveIntent.Login -> state
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application)
                val container = AppContainer(application)
                DiveViewModel(
                    locationClient = container.locationClient
                )
            }
        }
    }
}

private fun AppState.Initialized.Authorized.updateNav(newNav: NavScreen): AppState {
    return when (this) {
        is WithLocation -> this.copy(navScreen = newNav)
        is WithoutLocation -> this.copy(navScreen = newNav)
    }
}