package me.ezar.anemon.ui.screens.trip

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ezar.anemon.R
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.network.websocket.models.PassengerTripStatus
import me.ezar.anemon.service.AnemonService
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.screens.trip.components.DestinationDetailsSelectorCard
import me.ezar.anemon.ui.screens.trip.components.ErrorBottomSheet
import me.ezar.anemon.ui.screens.trip.components.MatchingCard
import me.ezar.anemon.ui.screens.trip.components.MatchingPassengersList
import me.ezar.anemon.ui.screens.trip.components.TripAction
import me.ezar.anemon.ui.screens.trip.components.TripStatusCard
import me.ezar.anemon.ui.utils.PreviewBoilerplate

enum class ScreenMode { PASSENGER, DRIVER }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DestinationSelectorScreen(onLogout: () -> Unit = {}, onExit: () -> Unit = {}) {
    val ctx = LocalContext.current
    var service by remember { mutableStateOf<AnemonService?>(null) }
    val haptic = LocalHapticFeedback.current

    val tripState by service?.tripState?.collectAsState() ?: remember { mutableStateOf(null) }
    val tripPolyline by service?.tripPolyline?.collectAsState() ?: remember {
        mutableStateOf(
            emptyList()
        )
    }
    val participantLocations by service?.participantLocations?.collectAsState()
        ?: remember { mutableStateOf(emptyMap()) }
    val isMatching by service?.isMatching?.collectAsState() ?: remember { mutableStateOf(false) }
    val incomingTripRequests by service?.incomingTripRequests?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val connectionError by service?.connectionError?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as AnemonService.AnemonBinder).getService().apply {
                    checkInitialState()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }
    }

    DisposableEffect(Unit) {
        Intent(ctx, AnemonService::class.java).also { intent ->
            ctx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        onDispose {
            ctx.unbindService(serviceConnection)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var profileError by remember { mutableStateOf<String?>(null) }

    val errorMessage = connectionError ?: profileError

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            sheetState.show()
        } else {
            if (sheetState.isVisible) {
                sheetState.hide()
            }
        }
    }

    if (errorMessage != null) {
        ErrorBottomSheet(
            sheetState = sheetState,
            errorMessage = errorMessage,
            onDismiss = onExit,
            dismissable = false
        )
    }


    Scaffold { contentPadding ->
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
            var uiHeight by remember { mutableStateOf(0.dp) }
            var showUi by remember { mutableStateOf(true) }
            val camState = rememberCameraPositionState()
            val polylines = remember { mutableStateListOf<LatLng>() }
            var currentMode by remember { mutableStateOf(ScreenMode.PASSENGER) }

            val middleCoords = camState.projection?.visibleRegion?.latLngBounds?.center
            var pickupLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
            var destinationLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
            var pickupAddress by remember { mutableStateOf("") }
            var destinationAddress by remember { mutableStateOf("") }
            var isPenjemputanSelected by remember { mutableStateOf(true) }
            var isCarSelected by remember { mutableStateOf(false) }
            var calculatedTariff by remember { mutableLongStateOf(-1L) }
            var isCalculating by remember { mutableStateOf(false) }

            var availableSlots by remember { mutableIntStateOf(1) }

            var lastKnownLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
            val locationClient = remember { LocationServices.getFusedLocationProviderClient(ctx) }

            val isInTrip = tripState != null

            LaunchedEffect(isInTrip, tripPolyline) {
                if (isInTrip) {
                    polylines.clear()
                    polylines.addAll(tripPolyline)
                    animateCameraToPolyline(camState, polylines)
                }
            }

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        val response = NetworkHandler.userService.getCurrentProfile()
                        when (response.status) {
                            HttpStatusCode.OK -> {
                                val profile = response.body<UserProfile>()
                                LocalStorage.cachedUserProfile = profile
                                if (profile.vehiclePreference == AccountVehiclePreference.PASSENGER) {
                                    currentMode = ScreenMode.PASSENGER
                                }
                            }

                            HttpStatusCode.Unauthorized -> {
                                profileError =
                                    "Kamu telah login di perangkat lain, mohon login ulang untuk melanjutkan"
                                LocalStorage.token = ""
                                LocalStorage.cachedUserProfile = null
                            }

                            HttpStatusCode.UpgradeRequired -> {
                                profileError =
                                    "Versi aplikasi sudah usang, silakan perbarui untuk melanjutkan"
                            }

                            else -> {
                                profileError =
                                    "Gagal mendapatkan profilmu: ${response.status}, coba login ulang"
                            }
                        }
                        LocalStorage.cachedUserProfile = response.body()

                    } catch (ex: Exception) {
                        profileError =
                            "Gagal dalam meminta profilmu ke server 😔, mungkin pilihan server salah atau server sedang down. (Exception: $ex.message)"
                    }
                }
                try {
                    locationClient.lastLocation.addOnSuccessListener {
                        if (it != null) {
                            lastKnownLocation = LatLng(it.latitude, it.longitude)
                            if (tripState == null) {
                                coroutineScope.launch {
                                    camState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            lastKnownLocation,
                                            15f
                                        ), 1500
                                    )
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    throw (ex)
                }
            }

            LaunchedEffect(tripState) {
                while (tripState != null) {
                    try {
                        locationClient.lastLocation.addOnSuccessListener {
                            if (it != null)
                                service?.sendLocation(LatLng(it.latitude, it.longitude))
                        }
                    } catch (ex: Exception) {
                        Log.e(
                            "DestinationSelectorScreen",
                            "Error occured while sending an updated location",
                            ex
                        )
                    }
                    delay(5000)
                }
            }

            val isDriverMatching = isMatching && currentMode == ScreenMode.DRIVER

            if (camState.isMoving && camState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE && !isInTrip) {
                polylines.clear()
                showUi = false
                calculatedTariff = -1L
            } else if (!camState.isMoving && polylines.isEmpty() && !isInTrip) {
                LaunchedEffect(camState.position.target) {
                    delay(300) // Debouncing
                    showUi = true
                    if (middleCoords != null && (!isCalculating && calculatedTariff == -1L)) {
                        withContext(Dispatchers.IO) {
                            val penjemputan = isPenjemputanSelected
                            val tempAddress = if (penjemputan) pickupAddress else destinationAddress

                            if (tempAddress != "Loading...") {
                                if (penjemputan) pickupAddress =
                                    "Loading..." else destinationAddress = "Loading..."
                                val response = try {
                                    NetworkHandler.geocodingService.reverseGeocode(
                                        latitude = middleCoords.latitude,
                                        longitude = middleCoords.longitude
                                    )
                                } catch (_: Exception) {
                                    if (penjemputan) pickupAddress =
                                        "Error getting address" else destinationAddress =
                                        "Error getting address"
                                    return@withContext
                                }
                                if (penjemputan) {
                                    pickupAddress = response.formattedAddress
                                    pickupLatLng =
                                        LatLng(middleCoords.latitude, middleCoords.longitude)
                                } else {
                                    destinationAddress = response.formattedAddress
                                    destinationLatLng =
                                        LatLng(middleCoords.latitude, middleCoords.longitude)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                        }
                    }
                }
            }

            val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }
            val mapSettings by remember(isMatching, isInTrip) {
                mutableStateOf(
                    MapUiSettings(
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = !isMatching && !isInTrip,
                        zoomGesturesEnabled = !isMatching && !isInTrip
                    )
                )
            }

            GoogleMap(
                modifier = Modifier,
                contentPadding = PaddingValues(bottom = uiHeight),
                cameraPositionState = camState,
                uiSettings = mapSettings,
                properties = mapProperties,
            ) {
                Polyline(polylines.toList(), color = MaterialTheme.colorScheme.primary, width = 8f)

                participantLocations.forEach { (user, location) ->
                    val isDriver = tripState?.driver?.email == user.email
                    Marker(
                        state = MarkerState(position = location),
                        title = user.name,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isDriver) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = polylines.isEmpty() && !isInTrip,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = uiHeight)
                        .fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Column {
                        Image(painterResource(R.drawable.baseline_location_on_48), null)
                        Spacer(modifier = Modifier.padding(bottom = 48.dp))
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(contentPadding)
            ) {
                val currentAction = if (currentMode == ScreenMode.PASSENGER) {
                    if (isCalculating) TripAction.CALCULATING
                    else if (calculatedTariff == -1L) TripAction.CALCULATE_ROUTE_PASSENGER
                    else TripAction.SEARCH_PASSENGER
                } else {
                    if (isCalculating) TripAction.CALCULATING
                    else if (polylines.isEmpty()) TripAction.CALCULATE_ROUTE_DRIVER
                    else if (isDriverMatching) TripAction.STOP_DRIVER_TRIP
                    else TripAction.DO_DRIVER_TRIP
                }
                LaunchedEffect(currentAction) {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                }

                AnimatedVisibility(!isMatching && !isInTrip && (LocalStorage.cachedUserProfile?.vehiclePreference != AccountVehiclePreference.PASSENGER)) {
                    SingleChoiceSegmentedButtonRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SegmentedButton(
                            selected = currentMode == ScreenMode.PASSENGER,
                            onClick = {
                                currentMode = ScreenMode.PASSENGER
                                polylines.clear()
                                calculatedTariff = -1L
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("Penumpang") }
                        SegmentedButton(
                            selected = currentMode == ScreenMode.DRIVER,
                            onClick = {
                                currentMode = ScreenMode.DRIVER
                                isPenjemputanSelected = false
                                polylines.clear()
                                calculatedTariff = -1L
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("Pengemudi") }
                    }
                }

                Spacer(Modifier.weight(1f))

                when {
                    isInTrip -> {
                        val isCurrentUserDriver =
                            tripState?.driver?.uid == LocalStorage.cachedUserProfile?.uid
                        var currentTarget by remember { mutableStateOf<LatLng?>(null) }
                        var isActionButtonEnabled by remember { mutableStateOf(true) }

                        LaunchedEffect(participantLocations, lastKnownLocation, tripState) {
                            if (currentMode == ScreenMode.DRIVER) {
                                val passengerTripDetails = tripState!!.passengers.values.first()

                                currentTarget = when (passengerTripDetails.status) {
                                    PassengerTripStatus.WAITING_FOR_PICKUP ->
                                        passengerTripDetails.pickupPoint.toLatLng()

                                    PassengerTripStatus.IN_TRANSIT ->
                                        passengerTripDetails.destinationPoint.toLatLng()

                                    else -> null
                                }
                            }


                            isActionButtonEnabled = run {
                                val result = FloatArray(1)
                                Location.distanceBetween(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude,
                                    currentTarget?.latitude ?: 0.0,
                                    currentTarget?.longitude ?: 0.0,
                                    result
                                )
                                Log.d(
                                    "DestinationSelectorScreen",
                                    "Distance to target: ${result[0]} meters"
                                )
                                return@run currentTarget != null && result[0] < 100f
                            }
                        }

                        tripState?.let {
                            TripStatusCard(
                                tripState = it,
                                isCurrentUserDriver = isCurrentUserDriver,
                                onSizeChange = { newHeight -> uiHeight = newHeight },
                                onPassengerAction = { passengerToUpdate ->
                                    val details = it.passengers[passengerToUpdate]
                                    if (details?.status == PassengerTripStatus.WAITING_FOR_PICKUP) {
                                        service?.pickupPassenger(passengerToUpdate)
                                    } else if (details?.status == PassengerTripStatus.IN_TRANSIT) {
                                        service?.dropoffPassenger(passengerToUpdate)
                                    }
                                },
                                isActionButtonEnabled = isActionButtonEnabled,
                                onEndTrip = { service?.disconnectFromTrip() },
                                onDismiss = { service?.disconnectFromTrip() },
                                onRequestCancel = {
                                    Toast.makeText(
                                        ctx,
                                        "Pembatalan perjalanan butuh persetujuan kedua belah pihak, pastikan pihak lain juga menekan tombol \"Batalkan\" yaa!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    service?.requestTripCancellation()
                                }
                            )
                        }
                    }

                    isMatching -> {

                        if (isDriverMatching) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                MatchingPassengersList(
                                    requests = incomingTripRequests,
                                    contentPadding = PaddingValues(bottom = uiHeight)
                                ) { passengerProfile ->
                                    service?.acceptTrip(passengerProfile)
                                }
                                MatchingCard(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    isDriver = true,
                                    onCancel = {
                                        service?.stopMatching()
                                        showUi = true
                                        polylines.clear()
                                        calculatedTariff = -1L
                                    },
                                    onSizeChange = { newHeight ->
                                        uiHeight = newHeight
                                        coroutineScope.launch {
                                            Log.d(
                                                "CameraPosition",
                                                "Changed camera focus because size change"
                                            )
                                            animateCameraToPolyline(camState, polylines)
                                        }
                                    }
                                )
                            }
                        } else { // Sebagai Passenger
                            MatchingCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                isDriver = false,
                                onCancel = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    service?.stopMatching()
                                    polylines.clear()
                                    calculatedTariff = -1L
                                },
                                onSizeChange = { newHeight ->
                                    uiHeight = newHeight
                                    coroutineScope.launch {
                                        animateCameraToPolyline(
                                            camState,
                                            polylines
                                        )
                                    }
                                }
                            )
                        }
                    }

                    else -> { // Pre-trip / Selection
                        AnimatedVisibility(
                            visible = showUi,
                            enter = slideInVertically { h -> h } + expandVertically(expandFrom = Alignment.Bottom),
                            exit = slideOutVertically { h -> h }
                        ) {
                            val userVehicle = LocalStorage.cachedUserProfile?.vehiclePreference
                            val maxSlots = if (userVehicle == AccountVehiclePreference.CAR) 4 else 1
                            val isSlotSelectorEnabled = userVehicle == AccountVehiclePreference.CAR

                            DestinationDetailsSelectorCard(
                                calculatedTariff = calculatedTariff,
                                pickupAddress = pickupAddress,
                                destinationAddress = destinationAddress,
                                isCarSelected = isCarSelected,
                                activeAddress = isPenjemputanSelected,
                                onSizeChange = { uiHeight = it },
                                onSelectedVehicleChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    if (it) {
                                        Toast.makeText(
                                            ctx,
                                            "Sepurane rekk, fitur iki gak iso digawe sek an 😔",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isCarSelected = false
                                    polylines.clear()
                                    calculatedTariff = -1L
                                },
                                onActiveAddressChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    isPenjemputanSelected = it
                                },
                                isActionButtonEnabled = if (currentMode == ScreenMode.DRIVER) {
                                    currentAction != TripAction.DO_DRIVER_TRIP || polylines.isNotEmpty()
                                } else true,
                                availableSlots = availableSlots,
                                maxSlots = maxSlots,
                                isSlotSelectorEnabled = isSlotSelectorEnabled,
                                onSlotsChanged = { newSlots -> availableSlots = newSlots },
                                onActionButtonClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    when (currentAction) {
                                        TripAction.CALCULATE_ROUTE_PASSENGER, TripAction.CALCULATE_ROUTE_DRIVER -> {
                                            if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                                                polylines.clear(); calculatedTariff =
                                                    -1L; isCalculating = true
                                                coroutineScope.launch {
                                                    val response =
                                                        NetworkHandler.routingService.calculateRoutes(
                                                            origin = if (currentMode == ScreenMode.PASSENGER) pickupLatLng else lastKnownLocation,
                                                            destination = destinationLatLng,
                                                            vehiclePreference = if (isCarSelected) AccountVehiclePreference.CAR else AccountVehiclePreference.MOTORCYCLE
                                                        )
                                                    if (currentMode == ScreenMode.PASSENGER)
                                                        calculatedTariff = response.tariffRupiah
                                                    polylines.addAll(PolyUtil.decode(response.encodedPolyline))
                                                    isCalculating = false
                                                    animateCameraToPolyline(camState, polylines)
                                                }
                                            }
                                        }

                                        TripAction.SEARCH_PASSENGER -> {
                                            showUi = false
                                            service?.startMatchingAsPassenger(
                                                vehicle = if (isCarSelected) AccountVehiclePreference.CAR else AccountVehiclePreference.MOTORCYCLE,
                                                pickupPoint = pickupLatLng,
                                                destinationPoint = destinationLatLng
                                            )
                                        }

                                        TripAction.DO_DRIVER_TRIP -> {
                                            showUi = false
                                            service?.startMatchingAsDriver(
                                                route = polylines.toList(),
                                                availableSlots = availableSlots,
                                            )
                                        }

                                        TripAction.CALCULATING -> {}
                                        else -> {}
                                    }
                                },
                                tripButtonAction = currentAction
                            )
                        }
                    }
                }
            }

            Box(
                contentAlignment = Alignment.TopStart, modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 84.dp)
            ) {
                AnimatedVisibility(!isMatching && !isInTrip) {
                    FloatingActionButton(onClick = onLogout) {
                        Icon(painterResource(R.drawable.baseline_exit_to_app_24), "Logout")
                    }
                }
            }
        }
    }
}

suspend fun animateCameraToPolyline(
    camState: CameraPositionState,
    polylines: List<LatLng>,
    duration: Int = 1000
) {
    if (polylines.isNotEmpty()) {
        val boundsBuilder = LatLngBounds.Builder()
        polylines.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        camState.animate(
            CameraUpdateFactory.newLatLngBounds(bounds, 150), // Increased padding
            duration
        )
    }
}


@Preview
@Composable
fun DestinationSelectorScreenPreview() {
    PreviewBoilerplate {
        DestinationSelectorScreen()
    }
}