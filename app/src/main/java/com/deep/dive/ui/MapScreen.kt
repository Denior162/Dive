package com.deep.dive.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deep.dive.R
import com.deep.dive.domain.model.DiveSpot
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.LoginStatus
import com.deep.dive.ui.state.MapFocus
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.theme.DiveTheme
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.animation.MapAnimationOptions


@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}


@Composable
fun LoginScreen(
    status: LoginStatus,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFormDisabled = status is LoginStatus.Loading
    val isSubmitEnabled = status is LoginStatus.ReadyToSubmit || status is LoginStatus.ServerError
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column {
            TextField(
                emailState,
                enabled = !isFormDisabled,
                isError = status is LoginStatus.InvalidFormat && status.emailError != null,
                label = { Text("Email") },
                supportingText = {
                    if (status is LoginStatus.InvalidFormat && status.emailError != null) {
                        Text(status.emailError, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            SecureTextField(passwordState, enabled = !isFormDisabled, supportingText = {
                if (status is LoginStatus.InvalidFormat && status.emailError != null) {
                    Text(status.emailError, color = MaterialTheme.colorScheme.error)
                }
            })
            Button(
                onClick = onLoginClick,
                modifier = modifier,
                enabled = isSubmitEnabled
            ) {
                Text("Login")
            }
            when (status) {
                is LoginStatus.Idle, is LoginStatus.ReadyToSubmit, is LoginStatus.InvalidFormat -> {}

                is LoginStatus.Loading -> {
                    LinearProgressIndicator()
                }

                is LoginStatus.ServerError -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = status.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Text("some_settings", modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: AppState.Initialized.Authorized,
    onPointClicked: (DiveSpot) -> Unit,
    onSheetDismissed: () -> Unit,

    ) {
    val cameraPositionState = rememberMapViewportState()
    val currentFocus = (state.navScreen as? NavScreen.MapView)?.focus

    LaunchedEffect(currentFocus) {
        when (currentFocus) {
            is MapFocus.User -> {
                cameraPositionState.flyTo(
                    cameraOptions = CameraOptions.Builder()
                        .center(currentFocus.point)
                        .zoom(12.0)
                        .build(),
                    animationOptions = MapAnimationOptions.mapAnimationOptions { duration(1500) }
                )
            }

            is MapFocus.Spot -> {
                cameraPositionState.flyTo(
                    cameraOptions = CameraOptions.Builder()
                        .center(currentFocus.diveSpot.coordinates)
                        .zoom(14.0)
                        .build(),
                    animationOptions = MapAnimationOptions.mapAnimationOptions { duration(1000) }
                )
            }

            MapFocus.Manual, null -> {
            }
        }
    }

    Map(state.diveSpots, onPointClicked, cameraPositionState)
    val nav = state.navScreen
    if (nav is NavScreen.MapView.SheetOpened) {
        ModalBottomSheet(onSheetDismissed) {
            val spot = nav.spot
            Text("${spot.coordinates.latitude()}, ${spot.coordinates.longitude()}")
        }
    }
}


@Composable
private fun Map(
    spots: List<DiveSpot>,
    onPointClicked: (DiveSpot) -> Unit,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier
) {

    val marker = rememberIconImage(
        key = "android-marker", painter = painterResource(id = R.drawable.ic_launcher_foreground)
    )

    MapboxMap(modifier.fillMaxSize(), mapViewportState = mapViewportState, scaleBar = {
        ScaleBar(Modifier.padding(top = 60.dp))
    }, logo = {
        Logo(Modifier.padding(bottom = 40.dp))
    }, attribution = {
        Attribution(Modifier.padding(bottom = 40.dp))
    }) {
        spots.forEach { spot ->
            PointAnnotation(point = spot.coordinates) {
                iconImage = marker
                interactionsState.onClicked {
                    onPointClicked(spot)
                    println("hi")
                    true
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    DiveTheme {}
}