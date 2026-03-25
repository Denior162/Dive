package com.deep.dive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.dive.ui.theme.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiveViewModel(): ViewModel() {
    private val _state = MutableStateFlow<AppState>(AppState.Uninitialized)

    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _state.value = AppState.Initialized.Unauthorized
        }
    }

    fun onLogin() {
        _state.value = AppState.Initialized.Authorized.MapView
    }

}