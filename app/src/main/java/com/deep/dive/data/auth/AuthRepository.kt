package com.deep.dive.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.deep.dive.ui.state.AuthDomainState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class AuthRepository(private val dataStore: DataStore<Preferences>) {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")

    val authState: Flow<AuthDomainState> = dataStore.data
        .map { preferences ->
            val token = preferences[TOKEN_KEY]
            if (token.isNullOrBlank()) {
                AuthDomainState.LoggedOut
            } else {
                AuthDomainState.LoggedIn(token)
            }
        }
        .onStart { emit(AuthDomainState.Checking) }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    suspend fun getToken(): String? {
        return authState
            .filter { it !is AuthDomainState.Checking }
            .first()
            .let { state ->
                if (state is AuthDomainState.LoggedIn) state.token else null
            }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
    }
}