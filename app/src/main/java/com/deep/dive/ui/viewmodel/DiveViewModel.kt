package com.deep.dive.ui.viewmodel

import android.annotation.SuppressLint
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.deep.dive.AppContainer
import com.deep.dive.DiveApplication
import com.deep.dive.data.DiveData
import com.deep.dive.domain.model.LoginRequest
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.AppState.Initialized.Authorized.WithLocation
import com.deep.dive.ui.state.AppState.Initialized.Authorized.WithoutLocation
import com.deep.dive.ui.state.AuthDomainState
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.LocationPrecision
import com.deep.dive.ui.state.LocationReason
import com.deep.dive.ui.state.LoginStatus
import com.deep.dive.ui.state.MapFocus
import com.deep.dive.ui.state.NavScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.geojson.Point
import kotlinx.coroutines.tasks.await
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class DiveViewModel(
    private val locationClient: FusedLocationProviderClient,
    private val appContainer: AppContainer
) : ContainerHost<AppState, DiveSideEffect>, ViewModel() {
    override val container = container<AppState, DiveSideEffect>(AppState.Uninitialized) {

        observeAuthStatus()
    }

    private fun observeAuthStatus() = intent {
        appContainer.authRepository.authState.collect { domainState ->
            when (domainState) {
                is AuthDomainState.Checking -> {}
                is AuthDomainState.LoggedOut -> {
                    reduce { AppState.Initialized.Unauthorized() }
                }

                is AuthDomainState.LoggedIn -> {
                    if (state !is AppState.Initialized.Authorized) {
                        fetchProfileAndInitialize()
                    }
                }
            }
        }
    }

    private fun fetchProfileAndInitialize() = intent {
        val profileResult = appContainer.apiService.getUserProfile()
        profileResult.fold(
            ifLeft = { profileError ->
                postSideEffect(DiveSideEffect.Toast("Failed to fetch profile: ${profileError.message}"))
                appContainer.authRepository.clearToken()
            },
            ifRight = { userProfile ->
                reduce {
                    WithoutLocation(
                        user = userProfile,
                        reason = LocationReason.Pending,
                        diveSpots = DiveData.points,
                        navScreen = NavScreen.MapView.SheetClosed(focus = MapFocus.Manual)
                    )
                }
            }
        )
    }

    val textFieldStateEmail = TextFieldState()
    val textFieldStatePassword = TextFieldState()
    fun onIntent(action: DiveIntent) {
        intent {
            when (action) {
                is DiveIntent.Login -> {
                    reduce {
                        if (state is AppState.Initialized.Unauthorized) {
                            (state as AppState.Initialized.Unauthorized).copy(status = LoginStatus.Loading)
                        } else state
                    }

                    val result = appContainer.apiService.login(
                        LoginRequest(action.email, action.password)
                    )

                    result.fold(
                        ifLeft = { error ->
                            reduce {
                                if (state is AppState.Initialized.Unauthorized) {
                                    (state as AppState.Initialized.Unauthorized).copy(
                                        status = LoginStatus.ServerError(error.message ?: "Unknown error")
                                    )
                                } else state
                            }
                                 },
                        ifRight = { authResponse ->
                            appContainer.authRepository.saveToken(authResponse.token)
                        }
                    )
                }

                is DiveIntent.PermissionGranted -> fetchRealLocation(action.precision)

                else -> reduce { reduceLogic(state, action) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchRealLocation(precision: LocationPrecision) = intent {
        try {
            val location = locationClient.lastLocation.await()

            if (location != null) {
                val realPoint = Point.fromLngLat(location.longitude, location.latitude)
                reduce { reduceLogic(state, DiveIntent.LocationSuccess(realPoint, precision)) }
            } else {
                reduce { reduceLogic(state, DiveIntent.LocationDenied) }
            }
        } catch (_: Exception) {
            reduce { reduceLogic(state, DiveIntent.LocationDenied) }
        }
    }

    private fun reduceLogic(
        state: AppState,
        intent: DiveIntent
    ): AppState {
        if (state !is AppState.Initialized.Authorized) {
            return state
        }
        return when (intent) {
            is DiveIntent.PermissionGranted -> state
            is DiveIntent.SheetClose -> state.updateNav(NavScreen.MapView.SheetClosed(focus = MapFocus.Manual))
            is DiveIntent.Select -> state.updateNav(NavScreen.MapView.SheetOpened(intent.spot))
            is DiveIntent.RequestLocation -> WithoutLocation(
                user = state.user,
                reason = LocationReason.Pending,
                diveSpots = state.diveSpots,
                navScreen = state.navScreen
            )

            is DiveIntent.LocationSuccess -> WithLocation(
                user = state.user,
                userPoint = intent.userPoint,
                precision = intent.precision,
                diveSpots = state.diveSpots,
                navScreen = state.navScreen
            )

            is DiveIntent.LocationDenied -> WithoutLocation(
                user = state.user,
                reason = LocationReason.Denied,
                diveSpots = state.diveSpots,
                navScreen = state.navScreen
            )

            is DiveIntent.Login -> state
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DiveApplication)
                DiveViewModel(
                    locationClient = app.container.locationClient,
                    appContainer = app.container
                )
            }
        }
    }
}

private fun AppState.Initialized.Authorized.updateNav(newNav: NavScreen): AppState =
    when (this) {
        is WithLocation -> this.copy(navScreen = newNav)
        is WithoutLocation -> this.copy(navScreen = newNav)
    }


sealed class DiveSideEffect {
    data class Toast(val message: String) : DiveSideEffect()
}