package me.ezar.anemon.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.models.requests.UserStatus
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.network.websocket.MatchingService
import me.ezar.anemon.network.websocket.TripService
import me.ezar.anemon.network.websocket.models.TripRequestMessage
import me.ezar.anemon.network.websocket.models.TripState
import me.ezar.anemon.session.LocalStorage

class AnemonService : Service() {
    private val binder = AnemonBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository = UserRepository()

    // WebSocket Service
    private lateinit var matchingService: MatchingService
    private lateinit var tripService: TripService

    // State Flow buat ui
    private val _isMatching = MutableStateFlow(false)
    val isMatching = _isMatching.asStateFlow()

    private val _incomingTripRequests = MutableStateFlow<List<TripRequestMessage>>(emptyList())
    val incomingTripRequests = _incomingTripRequests.asStateFlow()

    private val _tripState = MutableStateFlow<TripState?>(null)
    val tripState = _tripState.asStateFlow()

    private val _tripPolyline = MutableStateFlow<List<LatLng>>(emptyList())
    val tripPolyline = _tripPolyline.asStateFlow()

    private val _participantLocations = MutableStateFlow<Map<UserProfile, LatLng>>(emptyMap())
    val participantLocations = _participantLocations.asStateFlow()

    private val _connectionError = MutableSharedFlow<String>()
    val connectionError = _connectionError.asSharedFlow()

    private var matchingJob: Job? = null
    private var tripConnectionJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        matchingService = MatchingService(NetworkHandler.apiClient)
        tripService = TripService(NetworkHandler.apiClient)

        collectTripServiceUpdates()
    }

    inner class AnemonBinder : Binder() {
        fun getService(): AnemonService = this@AnemonService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun collectTripServiceUpdates() {
        serviceScope.launch {
            tripService.tripState.collect {
                _tripState.value = it
            }
        }
        serviceScope.launch {
            tripService.locationUpdates.collect { update ->
                _participantLocations.value = _participantLocations.value.toMutableMap().apply {
                    this[update.sender] = update.location
                }
            }
        }
        serviceScope.launch {
            tripService.polylineUpdates.collect {
                _tripPolyline.value = it
            }
        }
    }

    fun checkInitialState() {
        if (_tripState.value != null || _isMatching.value) return

        serviceScope.launch {
            userRepository.getUserState().onSuccess { response ->
                if (response.status == UserStatus.IN_TRIP_AS_DRIVER || response.status == UserStatus.IN_TRIP_AS_PASSENGER) {
                    response.tripId?.let { connectToTrip(it) }
                } else if (response.status == UserStatus.IDLE) {
                    Log.i("AnemonService", "User is idle, no active trip.")
                }
            }.onFailure {
                LocalStorage.logout()
                _connectionError.emit("${it.message}")
            }
        }
    }

    fun startMatchingAsPassenger(
        vehicle: AccountVehiclePreference,
        pickupPoint: LatLng,
        destinationPoint: LatLng
    ) {
        if (matchingJob?.isActive == true) return
        _isMatching.value = true
        startForeground(
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.buildForegroundNotification(this, "Finding a match...")
        )

        matchingJob = serviceScope.launch {
            try {
                matchingService.listenAsPassenger(
                    vehicle = vehicle,
                    pickupPoint = pickupPoint,
                    destinationPoint = destinationPoint
                ) { _, tripId ->
                    NotificationHelper.showMatchFoundNotification(applicationContext)
                    connectToTrip(tripId)
                    stopMatching()
                }
            } catch (e: Exception) {
                if (e is CancellationException) Log.i(
                    "AnemonService",
                    "Passenger matching cancelled."
                )
                if (e.message!!.contains("expected status code 101 but was 401")) {
                    _connectionError.emit("Kamu telah login di perangkat lain, mohon login ulang untuk melanjutkan")
                    LocalStorage.logout()
                } else _connectionError.emit("Failed to find a match: ${e.message}")
            } finally {
                stopMatching()
            }
        }
    }

    fun startMatchingAsDriver(route: List<LatLng>, availableSlots: Int) {
        if (matchingJob?.isActive == true) return
        _isMatching.value = true
        _incomingTripRequests.value = emptyList()
        startForeground(
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.buildForegroundNotification(
                this,
                "You are online and finding passengers."
            )
        )

        matchingJob = serviceScope.launch {
            try {
                matchingService.listenAsDriver(
                    route = route,
                    availableSlots = availableSlots,
                    onTripRequest = { request ->
                        _incomingTripRequests.value += request
                        NotificationHelper.showMatchRequestNotification(applicationContext)
                    },
                    onMatchFound = { _, tripId ->
                        connectToTrip(tripId)
                        stopMatching()
                    },
                    onMatchCancel = { profile ->
                        _incomingTripRequests.value =
                            _incomingTripRequests.value.filterNot { it.passengerProfile == profile }
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) Log.i("AnemonService", "Driver matching cancelled.")
                if (e.message!!.contains("expected status code 101 but was 401")) {
                    _connectionError.emit("Kamu telah login di perangkat lain, mohon login ulang untuk melanjutkan")
                    LocalStorage.logout()
                } else {
                    _connectionError.emit("Failed to find passengers: ${e.message}")
                }
            } finally {
                stopMatching()
            }
        }
    }

    fun stopMatching() {
        serviceScope.launch {
            matchingService.stopMatching()
            matchingService.closeSession()
            matchingJob?.cancelAndJoin()
            matchingJob = null
            _isMatching.value = false
            _incomingTripRequests.value = emptyList()
            if (tripState.value == null) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    fun acceptTrip(passengerProfile: UserProfile) {
        serviceScope.launch {
            matchingService.acceptTrip(passengerProfile)
            _incomingTripRequests.value =
                _incomingTripRequests.value.filterNot { it.passengerProfile == passengerProfile }
        }
    }

    fun connectToTrip(tripId: String) {
        if (tripConnectionJob?.isActive == true) return

        startForeground(
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.buildForegroundNotification(this, "You're currently in a trip.")
        )

        tripConnectionJob = serviceScope.launch {
            try {
                tripService.connect(tripId)
            } catch (e: Exception) {
                if (e is CancellationException) Log.i("AnemonService", "Trip connection cancelled.")
                else {
                    Log.e("AnemonService", "Trip connection error", e)
                    _connectionError.emit("Trip connection lost: ${e.cause} ${e.message}")
                }
            } finally {
                _tripState.value = null
                _participantLocations.value = emptyMap()
                _tripPolyline.value = emptyList()
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    fun disconnectFromTrip() {
        serviceScope.launch {
            tripService.disconnect()
            tripConnectionJob?.cancelAndJoin()
            tripConnectionJob = null
            _tripState.value = null
            _participantLocations.value = emptyMap()
            _tripPolyline.value = emptyList()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    fun sendLocation(location: LatLng) {
        if (tripConnectionJob?.isActive == true) {
            serviceScope.launch { tripService.sendLocation(location) }
        }
    }

    fun pickupPassenger(passenger: UserProfile) {
        serviceScope.launch { tripService.pickupPassenger(passenger) }
    }

    fun dropoffPassenger(passenger: UserProfile) {
        serviceScope.launch { tripService.dropoffPassenger(passenger) }
    }

    fun requestTripCancellation() {
        serviceScope.launch { tripService.requestCancellation() }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}