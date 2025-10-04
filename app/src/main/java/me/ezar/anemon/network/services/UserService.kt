package me.ezar.anemon.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.ezar.anemon.BuildConfig
import me.ezar.anemon.models.requests.LoginRequest
import me.ezar.anemon.models.requests.UserCreateRequest

class UserService(val apiClient: HttpClient) {
    suspend fun createUser(
        dto: UserCreateRequest,
    ): HttpResponse {
        return apiClient.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }

    suspend fun loginUser(email: String, password: String): HttpResponse {
        return apiClient.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
    }

    suspend fun getCurrentProfile(): HttpResponse {
        return apiClient.get("/users/profile") {
            header("appVersion", BuildConfig.VERSION_CODE)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun getUserState(): HttpResponse {
        return apiClient.get("/users/state")
    }
}