package me.ezar.anemon.network.websocket.models

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler.configuredJson
import me.ezar.anemon.ui.utils.fromBase64
import me.ezar.anemon.ui.utils.toBase64

enum class TripAction {
    // Client -> Server
    JOIN_TRIP,
    UPDATE_LOCATION,
    PICKUP_PASSENGER,
    DROPOFF_PASSENGER,

    UPDATE_CANCELLATION,

    TRIP_STATE_UPDATE,
    POLYLINE_UPDATE,
    LOCATION_BROADCAST,
    ERROR,

    CANCEL_REQUEST_BROADCAST,

}

// --- Server Models ---

@Serializable
enum class PassengerTripStatus {
    WAITING_FOR_PICKUP,
    IN_TRANSIT,
    DROPPED_OFF
}

@Serializable
enum class TripStatus {
    AWAITING_PARTICIPANTS,
    EN_ROUTE_TO_PICKUP,
    RECONNECTING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
data class TripDetails(
    val pickupPoint: Point,
    val destinationPoint: Point,
    var status: PassengerTripStatus
) {
    companion object {
        fun mock() = TripDetails(
            pickupPoint = Point(0.0, 0.0),
            destinationPoint = Point(1.0, 1.0),
            status = PassengerTripStatus.WAITING_FOR_PICKUP
        )
    }
}

@Serializable
data class TripState(
    val tripId: String,
    val driver: UserProfile,
    val passengers: Map<UserProfile, TripDetails>,
    var status: TripStatus,
) {
    companion object {
        fun fromRawMessage(raw: String): TripState {
            val base64Json = raw.substringAfter(' ')
            return configuredJson.decodeFromString(serializer(), base64Json.fromBase64())
        }
    }
}

data class PolylineUpdateMessage(
    val encodedPolyline: String
) {
    companion object {
        fun fromRawMessage(raw: String): PolylineUpdateMessage {
            val polyline = raw.substringAfter(' ')
            return PolylineUpdateMessage(polyline)
        }
    }
}

data class TripLocationBroadcast(
    val sender: UserProfile,
    val location: LatLng
) {
    companion object {
        fun fromRawMessage(raw: String): TripLocationBroadcast {
            val parts = raw.split(" ")
            val profileJson = parts[1].fromBase64()
            val locationJson = parts[2].fromBase64()
            val sender = configuredJson.decodeFromString(UserProfile.serializer(), profileJson)
            val point = configuredJson.decodeFromString(Point.serializer(), locationJson)
            return TripLocationBroadcast(sender, LatLng(point.latitude, point.longitude))
        }
    }
}

fun createJoinTripMessage(tripId: String): String {
    return "${TripAction.JOIN_TRIP} $tripId"
}

fun createUpdateLocationMessage(location: Point): String {
    return "${TripAction.UPDATE_LOCATION} ${
        configuredJson.encodeToString(
            Point.serializer(),
            location
        )
    }"
}

fun createPassengerActionMessage(action: TripAction, passenger: UserProfile): String {
    val profileJson = configuredJson.encodeToString(UserProfile.serializer(), passenger)
    return "$action ${profileJson.toBase64()}"
}

fun createCancellationRequestMessage(): String {
    return TripAction.UPDATE_CANCELLATION.name
}