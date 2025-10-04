package me.ezar.anemon.models.requests

import kotlinx.serialization.Serializable

// --- Request Models (DTOs for client -> server) ---
@Serializable
data class ReverseGeocodingRequest(
    val latitude: Double,
    val longitude: Double
)

// --- Response Models (DTOs for server -> client) ---
@Serializable
data class ReverseGeocodingResponse(
    val formattedAddress: String
)