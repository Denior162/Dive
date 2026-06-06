package com.deep.dive.ui.state

sealed interface AuthDomainState {
    data object Checking : AuthDomainState
    data object LoggedOut : AuthDomainState
    data class LoggedIn(val token: String) : AuthDomainState
}