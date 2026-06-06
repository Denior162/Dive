package com.deep.dive

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.deep.dive.data.auth.AuthRepository
import com.deep.dive.domain.network.IseaApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AppContainer(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "auth_prefs")
    val authRepository: AuthRepository by lazy {
        AuthRepository(context.dataStore)
    }
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    authRepository.getToken()?.let { token ->
                        BearerTokens(token, "")
                    }
                }
            }
        }
    }

    val apiService: IseaApiService by lazy {
        IseaApiService(httpClient)
    }

    val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
}