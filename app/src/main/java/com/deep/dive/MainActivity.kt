package com.deep.dive

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.deep.dive.ui.DiveApp
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.LocationPrecision
import com.deep.dive.ui.state.LocationReason
import com.deep.dive.ui.theme.DiveTheme
import com.deep.dive.ui.viewmodel.DiveSideEffect
import com.deep.dive.ui.viewmodel.DiveViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

class MainActivity : ComponentActivity() {
    val viewModel: DiveViewModel by viewModels {
        DiveViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.container.stateFlow.value is AppState.Uninitialized
        }

        enableEdgeToEdge()
        setContent {

            val state by viewModel.collectAsState()
            val context = LocalContext.current
            viewModel.collectSideEffect { sideEffect ->
                when (sideEffect) {
                    is DiveSideEffect.Toast -> {
                        println(sideEffect.message)
                        android.widget.Toast.makeText(
                            context, sideEffect.message, android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }


            val locationPermissionRequest = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                        viewModel.onIntent(DiveIntent.PermissionGranted(LocationPrecision.PRECISE))
                    }

                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                        viewModel.onIntent(DiveIntent.PermissionGranted(LocationPrecision.COARSE))
                    }

                    else -> {
                        viewModel.onIntent(DiveIntent.LocationDenied)
                    }
                }
            }

            DiveTheme {
                val currentState = state

                if (currentState is AppState.Initialized.Authorized.WithoutLocation && currentState.reason is LocationReason.Pending) {

                    LaunchedEffect(Unit) {
                        locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
                DiveApp(viewModel)
            }
        }
    }
}