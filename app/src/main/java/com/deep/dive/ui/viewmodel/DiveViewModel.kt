package com.deep.dive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.dive.ui.DiveIntent
import com.deep.dive.ui.theme.AppState
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
                AppState.Initialized.Authorized.MapView.SheetClosed
            }

            is DiveIntent.SheetClose -> {
                if (state is AppState.Initialized.Authorized.MapView) {
                    AppState.Initialized.Authorized.MapView.SheetClosed
                } else state
            }

            is DiveIntent.Select -> {
                if (state is AppState.Initialized.Authorized.MapView) {
                    AppState.Initialized.Authorized.MapView.SheetOpened(intent.point)
                } else state
            }
        }
    }
}