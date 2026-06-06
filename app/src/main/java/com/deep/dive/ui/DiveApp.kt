package com.deep.dive.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.viewmodel.DiveViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DiveApp(viewModel: DiveViewModel) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { /* ... */ }

    DiveContent(
        state = state,
        onIntent = { intent -> viewModel.onIntent(intent) },
        emailState = viewModel.textFieldStateEmail,
        passwordState = viewModel.textFieldStatePassword
    )
}

@Composable
fun DiveContent(
    state: AppState,
    onIntent: (DiveIntent) -> Unit,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (state) {
            is AppState.Uninitialized -> LoadingScreen(Modifier.padding(innerPadding))
            is AppState.Initialized.Unauthorized -> LoginScreen(
                status = state.status,
                emailState = emailState, passwordState, onLoginClick = {
                    onIntent(
                        DiveIntent.Login(
                            emailState.text.toString(), passwordState.text.toString()
                        )
                    )
                }, Modifier.padding(innerPadding)
            )

            is AppState.Initialized.Authorized -> {
                when (state.navScreen) {
                    is NavScreen.MapView -> {
                        MapScreen(state, onPointClicked = { point ->
                            onIntent(DiveIntent.Select(point))
                        }, onSheetDismissed = { onIntent(DiveIntent.SheetClose) })
                    }

                    is NavScreen.Settings -> SettingsScreen()
                }
            }
        }
    }
}