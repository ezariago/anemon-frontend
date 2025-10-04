package me.ezar.anemon.ui.screens.trip.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.ezar.anemon.R
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.websocket.models.PassengerTripStatus
import me.ezar.anemon.network.websocket.models.TripDetails
import me.ezar.anemon.network.websocket.models.TripState
import me.ezar.anemon.network.websocket.models.TripStatus
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.profilePictureBuilder

@Composable
fun TripStatusCard(
    tripState: TripState,
    isCurrentUserDriver: Boolean,
    onSizeChange: (Dp) -> Unit,
    onPassengerAction: (UserProfile) -> Unit,
    isActionButtonEnabled: Boolean = true,
    onEndTrip: () -> Unit,
    onDismiss: () -> Unit,
    onRequestCancel: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var imageIdToShow by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    if (imageIdToShow != null) {
        ImageViewerDialog(
            imagePathId = imageIdToShow!!.first,
            isVehicleImage = imageIdToShow!!.second,
            onDismiss = { imageIdToShow = null }
        )
    }

    val onShowImage: (UserProfile, Boolean) -> Unit = { profile, isVehicle ->
        if (profile.uid <= 0) {
            Toast.makeText(context, "Gambar tidak tersedia untuk mock data.", Toast.LENGTH_SHORT)
                .show()
        } else {
            scope.launch {
                if (isVehicle) {
                    imageIdToShow = Pair(profile.vehicleImageId, true)
                } else {
                    imageIdToShow = Pair(profile.profilePictureId, false)
                }
            }
        }
    }

    when (tripState.status) {
        TripStatus.AWAITING_PARTICIPANTS -> AwaitingParticipantsCard(onSizeChange)
        TripStatus.RECONNECTING -> ReconnectingCard(onSizeChange)
        TripStatus.EN_ROUTE_TO_PICKUP, TripStatus.IN_PROGRESS -> TripInProgressCard(
            tripState = tripState,
            isCurrentUserDriver = isCurrentUserDriver,
            isActionButtonEnabled = isActionButtonEnabled,
            onPassengerAction = onPassengerAction,
            onSizeChange = onSizeChange,
            onRequestCancel = onRequestCancel,
            onShowImage = onShowImage
        )

        TripStatus.COMPLETED, TripStatus.CANCELLED -> TripEndedCard(
            tripState = tripState,
            onEndTrip = onEndTrip,
            onDismiss = onDismiss,
            onSizeChange = onSizeChange
        )
    }
}

@Composable
private fun AwaitingParticipantsCard(onSizeChange: (Dp) -> Unit) {
    BaseTripCard(onSizeChange = onSizeChange) {
        StatusTitle("Mempersiapkan Perjalanan...")
        Spacer(Modifier.height(16.dp))
        WaitingIndicator("Menunggu semua peserta terhubung...")
    }
}

@Composable
private fun ReconnectingCard(onSizeChange: (Dp) -> Unit) {
    BaseTripCard(onSizeChange = onSizeChange) {
        StatusTitle("Menghubungkan ulang...")
        Spacer(Modifier.height(16.dp))
        WaitingIndicator("Koneksi peserta lain terputus, minta dia untuk buka kembali aplikasinya!")
    }
}

@Composable
private fun TripInProgressCard(
    tripState: TripState,
    isCurrentUserDriver: Boolean,
    isActionButtonEnabled: Boolean,
    onPassengerAction: (UserProfile) -> Unit,
    onSizeChange: (Dp) -> Unit,
    onRequestCancel: () -> Unit,
    onShowImage: (UserProfile, Boolean) -> Unit
) {
    BaseTripCard(onSizeChange = onSizeChange) {
        if (isCurrentUserDriver) {
            DriverInProgressContent(
                tripState,
                isActionButtonEnabled,
                onPassengerAction,
                onRequestCancel,
                onShowImage
            )
        } else {
            PassengerInProgressContent(
                tripState,
                onRequestCancel,
                onShowImage
            )
        }
    }
}

@Composable
private fun TripEndedCard(
    tripState: TripState,
    onDismiss: () -> Unit,
    onEndTrip: () -> Unit,
    onSizeChange: (Dp) -> Unit,
) {
    BaseTripCard(onSizeChange = onSizeChange) {
        LaunchedEffect(Unit) {
            onEndTrip()
        }
        if (tripState.status == TripStatus.COMPLETED) {

            val statusText = buildAnnotatedString {
                val normalStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()

                withStyle(normalStyle) {
                    append("Pastiin gaada barang yang ketinggalan, dan terimakasih banyakk udah pake ANEMON! 😊😊")
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        "Perjalanan Selesai",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    Text(statusText, textAlign = TextAlign.Center)
                }

            }
        } else {
            StatusTitle("Perjalanan Dibatalkan")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Halaman Utama")
        }
    }
}

@Composable
private fun DriverInProgressContent(
    tripState: TripState,
    isActionButtonEnabled: Boolean,
    onPassengerAction: (UserProfile) -> Unit,
    onRequestCancel: () -> Unit,
    onShowImage: (UserProfile, Boolean) -> Unit
) {
    val ctx = LocalContext.current
    val passenger =
        tripState.passengers.keys.toList()[0]
    val imageRequest =
        if (passenger.uid <= 0)
            R.drawable.baseline_hail_24
        else profilePictureBuilder(
            context = ctx,
            profileId = passenger.profilePictureId
        )

    val myStatus = tripState.passengers[passenger]!!.status
    val statusText = buildAnnotatedString {
        val normalStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
        val boldStyle = normalStyle.copy(fontWeight = FontWeight.Bold)

        when (myStatus) {
            PassengerTripStatus.WAITING_FOR_PICKUP -> {
                withStyle(normalStyle) {
                    append("Pergi ke titik jemput, lalu minta penumpang untuk naik")
                }
            }

            PassengerTripStatus.IN_TRANSIT -> {
                withStyle(normalStyle) {
                    append("Cusss antar ")
                }
                withStyle(boldStyle) {
                    append(tripState.driver.name)
                }
                withStyle(normalStyle) {
                    append(" ke tujuannya!")
                }
            }

            PassengerTripStatus.DROPPED_OFF -> {
                withStyle(normalStyle) {
                    append("Kamu sudah sampai tujuan!")
                }
            }
        }
    }


    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.Start, modifier = Modifier
                .weight(1f)
                .height(100.dp), verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                passenger.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(statusText)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .height(100.dp)
                    .width(100.dp)
                    .clickable(enabled = passenger.uid > 0) {
                        onShowImage(passenger, false)
                    }
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Column {
        Button(
            onClick = onRequestCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Batalkan Perjalanan")
        }
        Spacer(Modifier.height(8.dp))
        DriverActionButton(tripState, isActionButtonEnabled, onPassengerAction)
    }
}

@Composable
private fun PassengerInProgressContent(
    tripState: TripState,
    onRequestCancel: () -> Unit,
    onShowImage: (UserProfile, Boolean) -> Unit
) {

    val ctx = LocalContext.current
    val driver = tripState.driver
    val imageRequest =
        if (driver.uid <= 0)
            R.drawable.baseline_hail_24
        else profilePictureBuilder(
            context = ctx,
            profileId = driver.profilePictureId
        )

    val currentUserProfile = LocalStorage.cachedUserProfile
    val myStatus = tripState.passengers[currentUserProfile]!!.status
    val statusText = buildAnnotatedString {
        val normalStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
        val boldStyle = normalStyle.copy(fontWeight = FontWeight.Bold)

        when (myStatus) {
            PassengerTripStatus.WAITING_FOR_PICKUP -> {
                withStyle(normalStyle) {
                    append("Tunggu driver dateng buat jemput kamu, ya! ")
                }
            }

            PassengerTripStatus.IN_TRANSIT -> {
                withStyle(normalStyle) {
                    append("Lagi di perjalanan bareng ")
                }
                withStyle(boldStyle) {
                    append(tripState.driver.name)
                }
                withStyle(normalStyle) {
                    append(", sabar ya!")
                }
            }

            PassengerTripStatus.DROPPED_OFF -> {
                withStyle(normalStyle) {
                    append("Kamu sudah sampai tujuan!")
                }
            }
        }
    }


    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.Start, modifier = Modifier
                .weight(1f)
                .height(100.dp), verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                driver.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(statusText)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .height(100.dp)
                    .width(100.dp)
                    .clickable(enabled = driver.uid > 0) {
                        onShowImage(driver, false)
                    }
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { onShowImage(driver, true) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Lihat Foto Kendaraan Driver")
    }

    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onRequestCancel,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text("Batalkan Perjalanan")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverActionButton(
    tripState: TripState,
    isEnabled: Boolean,
    onPassengerAction: (UserProfile) -> Unit
) {
    val nextPassengerForAction =
        tripState.passengers.entries.firstOrNull { // Hrs e ini dipake buat kalau passengernya lebih dari 1 (kyk di mobil gitu), tapi skrg blm fully implemented jadi ya force ke 1 aja
            it.value.status == PassengerTripStatus.WAITING_FOR_PICKUP
        }?.key ?: tripState.passengers.entries.firstOrNull {
            it.value.status == PassengerTripStatus.IN_TRANSIT
        }?.key

    if (nextPassengerForAction != null) {
        val passenger = nextPassengerForAction
        val details =
            tripState.passengers[passenger]!!

        ConfirmationSlider(
            modifier = Modifier.fillMaxWidth(),
            text = when (details.status) {
                PassengerTripStatus.WAITING_FOR_PICKUP -> "Penumpang sudah naik!"
                PassengerTripStatus.IN_TRANSIT -> "Penumpang sudah sampai!"
                else -> "IllegalState (?)"
            },
            isEnabled = isEnabled,
            onSuccessfulSlide = {
                onPassengerAction(passenger)
            }
        )
    }
}

@Composable
private fun BaseTripCard(
    modifier: Modifier = Modifier,
    onSizeChange: (Dp) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val localDensity = LocalDensity.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onGloballyPositioned {
                onSizeChange(with(localDensity) { it.size.height.toDp() })
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun StatusTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        //fontWeight = FontWeight.Bold,
        //textAlign = TextAlign.Center
    )
}

@Composable
private fun WaitingIndicator(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(16.dp))
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun TripStatusCardPassengerEnRoutePreview() {
    PreviewBoilerplate {
        LocalStorage.cachedUserProfile = UserProfile.mock2()
        TripStatusCard(
            tripState = TripState(
                tripId = "trip123",
                driver = UserProfile.mock1(),
                passengers = mapOf(
                    UserProfile.mock2() to TripDetails.mock()
                ),
                status = TripStatus.EN_ROUTE_TO_PICKUP
            ),
            isCurrentUserDriver = false,
            onSizeChange = {},
            onPassengerAction = {},
            onEndTrip = {},
            onDismiss = {},
            onRequestCancel = {}
        )
    }
}

@Preview
@Composable
fun TripStatusCardDriverEnRoutePreview() {
    PreviewBoilerplate {
        LocalStorage.cachedUserProfile = UserProfile.mock1()
        TripStatusCard(
            tripState = TripState(
                tripId = "trip123",
                driver = UserProfile.mock1(),
                passengers = mapOf(
                    UserProfile.mock2() to TripDetails.mock()
                ),
                status = TripStatus.EN_ROUTE_TO_PICKUP
            ),
            isCurrentUserDriver = true,
            onSizeChange = {},
            onPassengerAction = {},
            onEndTrip = {},
            onDismiss = {},
            onRequestCancel = {}
        )
    }
}

@Preview
@Composable
fun TripStatusCardPassengerEndedPreview() {
    PreviewBoilerplate {
        LocalStorage.cachedUserProfile = UserProfile.mock2()
        TripStatusCard(
            tripState = TripState(
                tripId = "trip123",
                driver = UserProfile.mock1(),
                passengers = mapOf(
                    UserProfile.mock2() to TripDetails.mock()
                ),
                status = TripStatus.COMPLETED
            ),
            isCurrentUserDriver = false,
            onSizeChange = {},
            onPassengerAction = {},
            onEndTrip = {},
            onDismiss = {},
            onRequestCancel = {}
        )
    }
}

@Preview
@Composable
fun TripStatusCardAwaitingPreview() {
    PreviewBoilerplate {
        LocalStorage.cachedUserProfile = UserProfile.mock1()
        TripStatusCard(
            tripState = TripState(
                tripId = "trip123",
                driver = UserProfile.mock1(),
                passengers = mapOf(
                    UserProfile.mock2() to TripDetails.mock()
                ),
                status = TripStatus.AWAITING_PARTICIPANTS
            ),
            isCurrentUserDriver = true,
            onSizeChange = {},
            onPassengerAction = {},
            onEndTrip = {},
            onDismiss = {},
            onRequestCancel = {}
        )
    }
}

@Preview
@Composable
fun TripStatusCardReconnectingPreview() {
    PreviewBoilerplate {
        LocalStorage.cachedUserProfile = UserProfile.mock1()
        TripStatusCard(
            tripState = TripState(
                tripId = "trip123",
                driver = UserProfile.mock1(),
                passengers = mapOf(
                    UserProfile.mock2() to TripDetails.mock()
                ),
                status = TripStatus.RECONNECTING
            ),
            isCurrentUserDriver = true,
            onSizeChange = {},
            onPassengerAction = {},
            onEndTrip = {},
            onDismiss = {},
            onRequestCancel = {}
        )
    }
}