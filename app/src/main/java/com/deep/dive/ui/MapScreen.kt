package com.deep.dive.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deep.dive.R
import com.deep.dive.data.DiveData.points
import com.deep.dive.domain.model.DiveSpot
import com.deep.dive.ui.state.AppState
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
fun LoginScreen(modifier: Modifier = Modifier, onLoginClick: () -> Unit) {
    Button(onLoginClick, modifier) {
        Text("login")
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
    LaunchedEffect(state) {
        if (state is AppState.Initialized.Authorized.WithLocation) {
            cameraPositionState.flyTo(
                cameraOptions = CameraOptions.Builder()
                    .center(state.userPoint)
                    .zoom(12.0)
                    .build(),
                animationOptions = MapAnimationOptions.mapAnimationOptions { duration(1500) }
            )
        }
    }

    Map(points, onPointClicked, cameraPositionState)
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


    MapboxMap(
        modifier.fillMaxSize(),
mapViewportState = mapViewportState,

    //rememberMapViewportState {
//        setCameraOptions {
//            zoom(2.0)
//            center(Point.fromLngLat(-98.0, 39.5))
//            pitch(0.0)
//            bearing(0.0)
//        }
//    },
        scaleBar = {
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
    DiveTheme {
    }
}