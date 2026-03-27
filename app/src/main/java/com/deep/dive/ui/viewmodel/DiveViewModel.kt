package com.deep.dive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.dive.ui.state.AppState
import com.deep.dive.ui.state.AppState.Initialized.Authorized.WithoutLocation
import com.deep.dive.ui.state.DiveIntent
import com.deep.dive.ui.state.LocationReason
import com.deep.dive.ui.state.NavScreen
import com.deep.dive.ui.state.NavScreen.MapView.SheetOpened
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiveViewModel : ViewModel() {
    private val _state = MutableStateFlow<AppState>(AppState.Uninitialized)

    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _state.value = AppState.Initialized.Unauthorized
        }
    }

    fun onIntent(intent: DiveIntent) {
        _state.update { currentState ->
            reduce(currentState, intent)
        }
    }

    private fun reduce(
        state: AppState,
        intent: DiveIntent
    ): AppState {
        return when (intent) {
            is DiveIntent.Login -> {
                WithoutLocation(
                    reason = LocationReason.Pending,
                    navScreen = NavScreen.MapView.SheetClosed
                )
            }

            is DiveIntent.SheetClose -> {
                if (state is AppState.Initialized.Authorized) {
                    state.updateNav(NavScreen.MapView.SheetClosed)
                } else state
            }

            is DiveIntent.Select -> {
                if (state is AppState.Initialized.Authorized) {
                    state.updateNav(SheetOpened(intent.spot))
                } else state
            }
        }
    }
}
private fun AppState.Initialized.Authorized.updateNav(newNav: NavScreen): AppState {
    return when (this) {
        is AppState.Initialized.Authorized.WithLocation -> this.copy(navScreen = newNav)
        is WithoutLocation -> this.copy(navScreen = newNav)
    }
}