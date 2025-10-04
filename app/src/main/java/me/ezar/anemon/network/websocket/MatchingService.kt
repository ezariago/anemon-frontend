package me.ezar.anemon.network.websocket

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.isActive
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.network.websocket.models.LineSegment
import me.ezar.anemon.network.websocket.models.MatchCancelMessage
import me.ezar.anemon.network.websocket.models.MatchMessage
import me.ezar.anemon.network.websocket.models.MatchingAction
import me.ezar.anemon.network.websocket.models.Point
import me.ezar.anemon.network.websocket.models.RegisterDriverMessage
import me.ezar.anemon.network.websocket.models.RegisterPassengerMessage
import me.ezar.anemon.network.websocket.models.StopMatchingMessage
import me.ezar.anemon.network.websocket.models.TripAcceptMessage
import me.ezar.anemon.network.websocket.models.TripRequestMessage
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.utils.toFrameText

class MatchingService(val apiClient: HttpClient) {
    private var session: DefaultClientWebSocketSession? = null
    suspend fun listenAsPassenger(
        vehicle: AccountVehiclePreference,
        pickupPoint: LatLng,
        destinationPoint: LatLng,
        onMatchFound: (UserProfile, String) -> Unit
    ) {
        try {
            apiClient.wss(
                host = NetworkHandler.hosts[LocalStorage.selectedServer],
                path = "/routing/matching",
                request = {
                    header(HttpHeaders.Authorization, "Bearer ${LocalStorage.token}")
                }
            ) {
                session = this
                val registerPassengerMessage = RegisterPassengerMessage(
                    vehicle,
                    Point.fromLatLng(pickupPoint),
                    Point.fromLatLng(destinationPoint)
                )
                send(registerPassengerMessage.toWebsocketMessageString().toFrameText())
                for (message in incoming) {
                    val frame = message as? Frame.Text ?: continue
                    val rawMessage = frame.readText()
                    val response = rawMessage.split(" ")
                    try {
                        when (MatchingAction.valueOf(response[0])) {
                            MatchingAction.MATCH -> {
                                Log.d("Matching Websocket", "Match found")
                                val matchMessage = MatchMessage(rawMessage)
                                onMatchFound(matchMessage.userProfile, matchMessage.tripId)
                                close()
                                return@wss
                            }

                            else -> {
                                Log.d("Matching Websocket", "Received action: ${response[0]}")
                            }
                        }
                    } catch (_: IllegalArgumentException) {
                        Log.w("Matching Websocket", "Received unknown action: ${response[0]}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MatchingService", "Passenger WS Error", e)
            throw e
        }
    }

    suspend fun listenAsDriver(
        route: List<LatLng>,
        availableSlots: Int,
        onTripRequest: (TripRequestMessage) -> Unit,
        onMatchFound: (UserProfile, String) -> Unit,
        onMatchCancel: (UserProfile) -> Unit = {},
    ) {
        try {
            apiClient.wss(
                host = NetworkHandler.hosts[LocalStorage.selectedServer],
                path = "/routing/matching",
                request = {
                    header(HttpHeaders.Authorization, "Bearer ${LocalStorage.token}")
                }
            ) {
                session = this

                val registerDriverMessage =
                    RegisterDriverMessage(availableSlots, LineSegment.fromPolyline(route))
                send(registerDriverMessage.toWebsocketMessageString().toFrameText())

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val message = frame.readText()
                        Log.d("Driver Websocket", "Received: $message")
                        try {
                            val action = MatchingAction.valueOf(message.split(" ")[0])
                            when (action) {
                                MatchingAction.TRIP_REQUEST -> {
                                    val tripRequest = TripRequestMessage(message)
                                    onTripRequest(tripRequest)
                                }

                                MatchingAction.MATCH -> {
                                    val matchMessage = MatchMessage(message)
                                    onMatchFound(matchMessage.userProfile, matchMessage.tripId)
                                    close()
                                    return@wss
                                }

                                MatchingAction.MATCH_CANCEL -> {
                                    Log.d("Driver Websocket", "Match cancelled")
                                    val matchMessage = MatchCancelMessage(message)
                                    onMatchCancel(matchMessage.passengerProfile)
                                }

                                else -> {
                                    Log.w("Driver Websocket", "Received unhandled action: $action")
                                }
                            }
                        } catch (_: IllegalArgumentException) {
                            Log.w(
                                "Driver Websocket",
                                "Received unknown action: ${message.split(" ")[0]}"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MatchingService", "Driver WS Error", e)
            throw e
        }
    }

    suspend fun stopMatching() {
        if (session?.isActive == true) {
            try {
                session?.send(StopMatchingMessage.toWebsocketMessageString().toFrameText())
            } catch (e: Exception) {
                Log.w(
                    "MatchingService",
                    "Could not send stop message, session might be closing.",
                    e
                )
            }
        }
    }

    suspend fun acceptTrip(passengerProfile: UserProfile) {
        session?.send(
            TripAcceptMessage(
                passengerProfile = passengerProfile
            ).toWebsocketMessageString().toFrameText()
        )
    }

    suspend fun closeSession() {
        session?.close()
        session = null
    }

}