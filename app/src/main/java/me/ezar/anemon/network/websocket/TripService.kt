package me.ezar.anemon.network.websocket

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.util.decodeBase64String
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.network.websocket.models.Point
import me.ezar.anemon.network.websocket.models.PolylineUpdateMessage
import me.ezar.anemon.network.websocket.models.TripAction
import me.ezar.anemon.network.websocket.models.TripLocationBroadcast
import me.ezar.anemon.network.websocket.models.TripState
import me.ezar.anemon.network.websocket.models.createCancellationRequestMessage
import me.ezar.anemon.network.websocket.models.createJoinTripMessage
import me.ezar.anemon.network.websocket.models.createPassengerActionMessage
import me.ezar.anemon.network.websocket.models.createUpdateLocationMessage
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.utils.toFrameText

class TripService(private val apiClient: HttpClient) {
    private var session: DefaultClientWebSocketSession? = null

    private val _tripState = MutableSharedFlow<TripState>()
    val tripState = _tripState.asSharedFlow()

    private val _locationUpdates = MutableSharedFlow<TripLocationBroadcast>()
    val locationUpdates = _locationUpdates.asSharedFlow()

    private val _polylineUpdates = MutableSharedFlow<List<LatLng>>()
    val polylineUpdates = _polylineUpdates.asSharedFlow()

    suspend fun connect(tripId: String) {
        try {
            apiClient.wss(
                host = "${NetworkHandler.hosts[LocalStorage.selectedServer]}",
                path = "/routing/trip",
                request = { header(HttpHeaders.Authorization, "Bearer ${LocalStorage.token}") }
            ) {
                session = this
                send(createJoinTripMessage(tripId).toFrameText())
                Log.i("TripService", "Connected to trip $tripId")

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val rawMessage = frame.readText()
                        Log.d("TripService", "Received: $rawMessage")
                        val actionString = rawMessage.split(" ").getOrNull(0) ?: continue
                        val action = try {
                            TripAction.valueOf(actionString)
                        } catch (_: Exception) {
                            continue
                        }

                        when (action) {
                            TripAction.TRIP_STATE_UPDATE -> {
                                _tripState.emit(TripState.fromRawMessage(rawMessage))
                            }

                            TripAction.POLYLINE_UPDATE -> {
                                val message = PolylineUpdateMessage.fromRawMessage(rawMessage)
                                _polylineUpdates.emit(PolyUtil.decode(message.encodedPolyline.decodeBase64String()))
                            }

                            TripAction.LOCATION_BROADCAST -> {
                                _locationUpdates.emit(
                                    TripLocationBroadcast.fromRawMessage(
                                        rawMessage
                                    )
                                )
                            }

                            TripAction.ERROR -> {
                                throw InternalError("TripService error: ${rawMessage.removePrefix("ERROR ")}")
                            }

                            TripAction.CANCEL_REQUEST_BROADCAST -> {
                                Log.i(
                                    "TripService",
                                    "A cancellation request was broadcasted: $rawMessage"
                                )
                            }

                            else -> {
                                Log.w("TripService", "Received unhandled action: $action")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TripService", "Connection error: ${e.message}", e)
            throw e
        } finally {
            session = null
            Log.i("TripService", "Disconnected.")
        }
    }

    suspend fun sendLocation(location: LatLng) {
        session?.send(createUpdateLocationMessage(Point.fromLatLng(location)).toFrameText())
    }

    suspend fun pickupPassenger(passenger: UserProfile) {
        session?.send(
            createPassengerActionMessage(
                TripAction.PICKUP_PASSENGER,
                passenger
            ).toFrameText()
        )
    }

    suspend fun dropoffPassenger(passenger: UserProfile) {
        session?.send(
            createPassengerActionMessage(
                TripAction.DROPOFF_PASSENGER,
                passenger
            ).toFrameText()
        )
    }

    suspend fun requestCancellation() {
        session?.send(createCancellationRequestMessage().toFrameText())
    }

    suspend fun disconnect() {
        session?.close()
        session = null
    }
}