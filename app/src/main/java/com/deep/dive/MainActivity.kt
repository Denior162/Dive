package com.deep.dive

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.deep.dive.ui.LoadingScreen
import com.deep.dive.ui.LoginScreen
import com.deep.dive.ui.MapScreen
import com.deep.dive.ui.SettingsScreen
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.LocationPrecision
import com.deep.dive.ui.state.LocationReason
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.theme.DiveTheme
import com.deep.dive.ui.viewmodel.DiveViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DiveViewModel by viewModels {
                DiveViewModel.Factory
            }
            val state by viewModel.state.collectAsState()

            val locationPermissionRequest = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                        viewModel.onIntent(DiveIntent.PermissionGranted(LocationPrecision.PRECISE))                    }
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                        viewModel.onIntent(DiveIntent.PermissionGranted(LocationPrecision.COARSE))                    }
                    else -> {
                        viewModel.onIntent(DiveIntent.LocationDenied)
                    }
                }
            }

            DiveTheme {
                val currentState = state

                if (currentState is AppState.Initialized.Authorized.WithoutLocation &&
                    currentState.reason is LocationReason.Pending) {

                    LaunchedEffect(Unit) {
                        locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentState) {
                        is AppState.Uninitialized -> LoadingScreen(Modifier.padding(innerPadding))
                        is AppState.Initialized.Unauthorized -> LoginScreen(
                            Modifier.padding(
                                innerPadding
                            ), onLoginClick = { viewModel.onIntent(DiveIntent.Login) })

                        is AppState.Initialized.Authorized -> {
                            when(currentState.navScreen) {
                                is NavScreen.MapView -> {
                                    MapScreen(
                                        currentState,
                                        onPointClicked = { point ->
                                            viewModel.onIntent(DiveIntent.Select(point))
                                        },
                                        onSheetDismissed = { viewModel.onIntent(DiveIntent.SheetClose) })
                                }
                                is NavScreen.Settings -> SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}