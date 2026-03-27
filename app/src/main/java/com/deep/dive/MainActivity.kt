package com.deep.dive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.dive.ui.LoadingScreen
import com.deep.dive.ui.LoginScreen
import com.deep.dive.ui.MapScreen
import com.deep.dive.ui.SettingsScreen
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.theme.DiveTheme
import com.deep.dive.ui.viewmodel.DiveViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DiveViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            DiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentState = state
                    when (currentState) {
                        is AppState.Uninitialized -> LoadingScreen(Modifier.padding(innerPadding))
                        is AppState.Initialized.Unauthorized -> LoginScreen(
                            Modifier.padding(
                                innerPadding
                            ), onLoginClick = { viewModel.onIntent(DiveIntent.Login) })

                        is AppState.Initialized.Authorized -> {
                            when(val nav = currentState.navScreen) {
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