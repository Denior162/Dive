package com.deep.dive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
                        is AppState.Uninitialized -> LoadingScreen(Modifier.padding(innerPadding))
                        is AppState.Initialized.Unauthorized -> LoginScreen(Modifier.padding(innerPadding), onLoginClick = { viewModel.onLogin() })
                        is AppState.Initialized.Authorized.MapView -> Greeting()
                        else -> ErrorScreen(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen (modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}

@Composable
fun LoginScreen (modifier: Modifier = Modifier, onLoginClick: () -> Unit) {
    Button(onLoginClick,modifier) {
        Text("login")
    }
}

@Composable
fun ErrorScreen (modifier: Modifier = Modifier) {
    Text("some_error", modifier)
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    MapboxMap(
        modifier.fillMaxSize(),
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                zoom(2.0)
                center(Point.fromLngLat(-98.0, 39.5))
                pitch(0.0)
                bearing(0.0)
            }
        },
        scaleBar = {
            ScaleBar(Modifier.padding(top = 60.dp))
        },
        logo = {
            Logo(Modifier.padding(bottom = 40.dp))
        },
        attribution = {
            Attribution(Modifier.padding(bottom = 40.dp))
        }
    ) {
        val marker = rememberIconImage(
            key = "red-marker",
            painter = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        // Insert a PointAnnotation composable function with the geographic coordinate to the content of MapboxMap composable function.
        PointAnnotation(point = Point.fromLngLat(18.06, 59.31)) {
            iconImage = marker
            interactionsState.onClicked {
                // do something when clicked
                println("hi")
                true
            }.onLongClicked {
                // do something when long clicked
                true
            }.onDragged {
                // do something when dragged
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DiveTheme {
        Greeting()
    }
}