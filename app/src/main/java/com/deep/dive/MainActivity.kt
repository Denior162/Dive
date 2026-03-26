package com.deep.dive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.dive.data.DiveData.points
import com.deep.dive.ui.DiveIntent
import com.deep.dive.ui.theme.AppState
import com.deep.dive.ui.theme.DiveTheme
import com.deep.dive.ui.viewmodel.DiveViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DiveViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            DiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val currentState = state) {
                        AppState.Uninitialized -> LoadingScreen(Modifier.padding(innerPadding))
                        AppState.Initialized.Unauthorized -> LoginScreen(
                            Modifier.padding(
                                innerPadding
                            ), onLoginClick = { viewModel.onIntent(DiveIntent.Login) })


                        is AppState.Initialized.Authorized.MapView -> {
                            MapScreen(
                                currentState,
                                onPointClicked = { point ->
                                    viewModel.onIntent(DiveIntent.Select(point))
                                },
                                onSheetDismissed = { viewModel.onIntent(DiveIntent.SheetClose) })
                        }

                        AppState.Initialized.Authorized.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}

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
    state: AppState.Initialized.Authorized.MapView,
    onPointClicked: (Point) -> Unit,
    onSheetDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Map(points, onPointClicked, modifier)
    if(state is AppState.Initialized.Authorized.MapView.SheetOpened) {
        ModalBottomSheet(onSheetDismissed) {
            Text("${state.point.latitude()}, ${state.point.longitude()}")
        }
    }
}


@Composable
fun Map(points: List<Point>, onPointClicked: (Point) -> Unit, modifier: Modifier = Modifier) {

    val marker = rememberIconImage(
        key = "android-marker", painter = painterResource(id = R.drawable.ic_launcher_foreground)
    )


    MapboxMap(modifier.fillMaxSize(), mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(2.0)
            center(Point.fromLngLat(-98.0, 39.5))
            pitch(0.0)
            bearing(0.0)
        }
    }, scaleBar = {
        ScaleBar(Modifier.padding(top = 60.dp))
    }, logo = {
        Logo(Modifier.padding(bottom = 40.dp))
    }, attribution = {
        Attribution(Modifier.padding(bottom = 40.dp))
    }) {
        points.forEach { point ->
            PointAnnotation(point = point) {
                iconImage = marker
                interactionsState.onClicked {
                    onPointClicked(point)
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