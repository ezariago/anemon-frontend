package me.ezar.anemon.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.ezar.anemon.network.services.GeocodingService
import me.ezar.anemon.network.services.RoutingService
import me.ezar.anemon.network.services.UserService
import me.ezar.anemon.session.LocalStorage

object NetworkHandler {
    val hosts = mapOf(
        "MAN 1 Jember" to "anemon.man1jember.sch.id",
        "Develop (IP)" to "10.0.2.2:80",
    )

    val configuredJson = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    val apiClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(configuredJson)
        }
        install(Auth) {
            bearer {
                loadTokens {
                    if (LocalStorage.token.isNotEmpty()) {
                        BearerTokens(LocalStorage.token, "")
                    } else {
                        null
                    }
                }
            }
        }
        install(WebSockets)
        install(DefaultRequest)

        defaultRequest {
            url("https://${hosts[LocalStorage.selectedServer]}/")
        }
    }

    val userService = UserService(apiClient)
    val geocodingService = GeocodingService(apiClient)
    val routingService = RoutingService(apiClient)
}