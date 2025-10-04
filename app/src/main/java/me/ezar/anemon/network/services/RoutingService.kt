package me.ezar.anemon.network.services

import com.google.android.gms.maps.model.LatLng
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.RoutingPreviewRequest
import me.ezar.anemon.models.requests.RoutingPreviewResponse

class RoutingService(val apiClient: HttpClient) {
    suspend fun calculateRoutes(
        origin: LatLng,
        destination: LatLng,
        vehiclePreference: AccountVehiclePreference
    ): RoutingPreviewResponse {
        return apiClient.post("/routing/preview") {
            contentType(ContentType.Application.Json)
            setBody(
                RoutingPreviewRequest(
                    originLat = origin.latitude,
                    originLng = origin.longitude,
                    destinationLat = destination.latitude,
                    destinationLng = destination.longitude,
                    vehiclePreference = vehiclePreference
                )
            )
        }.body()
    }
}