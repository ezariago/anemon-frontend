package me.ezar.anemon.network.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.ezar.anemon.models.requests.ReverseGeocodingRequest
import me.ezar.anemon.models.requests.ReverseGeocodingResponse

class GeocodingService(val apiClient: HttpClient) {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodingResponse {
        return apiClient.post("/geocoding/reverse") {
            contentType(ContentType.Application.Json)
            setBody(ReverseGeocodingRequest(latitude, longitude))
        }.body()
    }
}