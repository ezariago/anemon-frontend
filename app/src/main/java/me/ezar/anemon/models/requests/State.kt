package me.ezar.anemon.models.requests

import kotlinx.serialization.Serializable

@Serializable
enum class UserStatus {
    IDLE,
    IN_TRIP_AS_DRIVER,
    IN_TRIP_AS_PASSENGER
}

@Serializable
data class UserStateResponse(
    val status: UserStatus,
    val tripId: String? = null
)