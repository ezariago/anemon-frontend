package me.ezar.anemon.ui.screens.trip.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import me.ezar.anemon.R
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserProfile
import me.ezar.anemon.network.NetworkHandler.hosts
import me.ezar.anemon.network.websocket.models.TripRequestMessage
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.utils.PreviewBoilerplate

@Composable
fun MatchingPassengersList(
    contentPadding: PaddingValues,
    requests: List<TripRequestMessage>,
    onPassengerSelected: (UserProfile) -> Unit
) {
    val ctx = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
            contentPadding = contentPadding
        ) {
            items(
                requests.size,
                key = { it }
            ) {
                val request = requests[it]
                val imageRequest = ImageRequest.Builder(ctx)
                    .data("https://${hosts[LocalStorage.selectedServer]}/images/${request.passengerProfile.profilePictureId}")
                    .httpHeaders(
                        NetworkHeaders.Builder()
                            .add("Authorization", "Bearer ${LocalStorage.token}")
                            .build()
                    )
                    .build()
                MatchingPassengerItem(
                    imageRequest = imageRequest,
                    request = request,
                    onClick = { onPassengerSelected(request.passengerProfile) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun MatchingPassengerItem(
    imageRequest: ImageRequest?,
    request: TripRequestMessage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passenger = request.passengerProfile
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier.clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.baseline_hail_24),
                        contentDescription = "Gambar Penumpang",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .clip(CircleShape)
                    )
                }
                Text(
                    passenger.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(2f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_share_location_24), null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.pickupAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_location_on_24), null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.destinationAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(onClick) {
                    Text(
                        "Terima - Rp. ${request.tariff}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MatchingPassengersListPreview() {
    PreviewBoilerplate {
        MatchingPassengersList(
            requests = listOf(
                TripRequestMessage(
                    passengerProfile = UserProfile(
                        uid = 1,
                        name = "John Doe",
                        email = "apip@apip.com",
                        nik = "1234567890",
                        vehiclePreference = AccountVehiclePreference.PASSENGER,
                        vehicleImageId = "1",
                        profilePictureId = "profile1"
                    ),
                    pickupAddress = "Jl Contoh 123, Jember, Kaliwates, Jawa Timur, Indonesia",
                    destinationAddress = "Jl Contoh 456, Jember, Kaliwates",
                    tariff = 10000L
                ),
                TripRequestMessage(
                    passengerProfile = UserProfile(
                        uid = 1,
                        name = "John Doe",
                        email = "apip@apip.com",
                        nik = "1234567890",
                        vehiclePreference = AccountVehiclePreference.PASSENGER,
                        vehicleImageId = "1",
                        profilePictureId = "profile1"
                    ),
                    pickupAddress = "Jl Contoh 123, Jember, Kaliwates, Jawa Timur, Indonesia",
                    destinationAddress = "Jl Contoh 456, Jember, Kaliwates",
                    tariff = 10000L
                )
            ),
            contentPadding = PaddingValues(0.dp),
            onPassengerSelected = {}
        )
    }
}

@Preview
@Composable
fun MatchingPassengerItemPreview() {
    PreviewBoilerplate {
        MatchingPassengerItem(
            imageRequest = null,
            request = TripRequestMessage(
                passengerProfile = UserProfile(
                    uid = 1,
                    name = "John Doe",
                    email = "john@gmail.com",
                    nik = "1234567890",
                    vehiclePreference = AccountVehiclePreference.PASSENGER,
                    profilePictureId = "profile1",
                    vehicleImageId = "1"
                ),
                pickupAddress = "Jl Contoh 123, Jember, Kaliwates, Jawa Timur, Indonesia",
                destinationAddress = "Jl Contoh 456, Jember, Kaliwates",
                tariff = 10000L
            ),
            onClick = {}
        )
    }
}