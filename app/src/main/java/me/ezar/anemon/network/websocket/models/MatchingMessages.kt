package me.ezar.anemon.network.websocket.models

import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler.configuredJson
import me.ezar.anemon.ui.utils.fromBase64
import me.ezar.anemon.ui.utils.toBase64

enum class MatchingAction {
    REGISTER_DRIVER,
    REGISTER_PASSENGER,
    TRIP_REQUEST,
    TRIP_ACCEPT,
    MATCH,
    MATCH_CANCEL,
    STOP_MATCHING,
    UPDATE_DRIVER_ROUTE
}

interface WebsocketMessage {
    val action: MatchingAction
    fun toWebsocketMessageString(): String
}

object StopMatchingMessage : WebsocketMessage {
    override val action = MatchingAction.STOP_MATCHING
    override fun toWebsocketMessageString() = action.name
}

data class RegisterPassengerMessage(
    val vehicle: AccountVehiclePreference,
    val pickupPoint: Point,
    val destinationPoint: Point
) : WebsocketMessage {
    override val action = MatchingAction.REGISTER_PASSENGER
    override fun toWebsocketMessageString(): String {
        return "$action $vehicle ${configuredJson.encodeToString(pickupPoint)} ${
            configuredJson.encodeToString(
                destinationPoint
            )
        }"
    }
}

data class RegisterDriverMessage(
    val availableSlots: Int, // Yaaaa harusnya bisa lebih dr 1, tpi krna mobil didisable jadinya motor doang wkwkwk
    val route: List<LineSegment>,
) : WebsocketMessage {
    override val action = MatchingAction.REGISTER_DRIVER

    override fun toWebsocketMessageString(): String {
        return "$action $availableSlots ${route.joinToString(" ") { "${it.start.latitude},${it.start.longitude}:${it.end.latitude},${it.end.longitude}" }}"
    }

}
data class TripRequestMessage(
    val passengerProfile: UserProfile,
    val pickupAddress: String,
    val destinationAddress: String,
    val tariff: Long,
) : WebsocketMessage {
    override val action = MatchingAction.TRIP_REQUEST

    override fun toWebsocketMessageString(): String {
        return "$action ${(configuredJson.encodeToString(passengerProfile)).toBase64()} ${pickupAddress.toBase64()} ${destinationAddress.toBase64()} $tariff"
    }

    constructor(formattedMessage: String) : this(
        passengerProfile = configuredJson.decodeFromString(
            UserProfile.serializer(),
            formattedMessage.split(" ")[1].fromBase64()
        ),
        pickupAddress = formattedMessage.split(" ")[2].fromBase64(),
        destinationAddress = formattedMessage.split(" ")[3].fromBase64(),
        tariff = formattedMessage.split(" ")[4].toLong()
    )
}

data class TripAcceptMessage(
    val passengerProfile: UserProfile,
) : WebsocketMessage {
    override val action = MatchingAction.TRIP_ACCEPT

    override fun toWebsocketMessageString(): String {
        return "$action ${configuredJson.encodeToString(passengerProfile).toBase64()}"
    }
}

data class MatchMessage(val tripId: String, val userProfile: UserProfile) : WebsocketMessage {
    override val action = MatchingAction.MATCH

    override fun toWebsocketMessageString(): String {
        return "$action $tripId ${configuredJson.encodeToString(userProfile).toBase64()}"
    }

    constructor(formattedMessage: String) : this(
        tripId = formattedMessage.split(" ")[1],
        userProfile = configuredJson.decodeFromString(
            UserProfile.serializer(),
            formattedMessage.split(" ")[2].fromBase64()
        )
    )
}

// SERVER -> CLIENT ONLY
data class MatchCancelMessage(
    val passengerProfile: UserProfile,
) : WebsocketMessage {
    override val action = MatchingAction.MATCH_CANCEL

    override fun toWebsocketMessageString(): String {
        return "$action ${configuredJson.encodeToString(passengerProfile).toBase64()}"
    }

    constructor(formattedMessage: String) : this(
        passengerProfile = configuredJson.decodeFromString(
            UserProfile.serializer(),
            formattedMessage.split(" ")[1].fromBase64()
        )
    )
}