package me.ezar.anemon.models.requests

import kotlinx.serialization.Serializable
import me.ezar.anemon.models.enum.AccountVehiclePreference

// --- DTO client -> server ---
@Serializable
data class RoutingPreviewRequest(
    val originLat: Double,
    val originLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val vehiclePreference: AccountVehiclePreference
)

// --- DTO server -> client ---
@Serializable
data class RoutingPreviewResponse(
    val encodedPolyline: String,
    val distanceMeters: Int,
    val tariffRupiah: Long,
)