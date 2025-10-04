package me.ezar.anemon.ui.screens.trip.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ezar.anemon.R
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.formatRupiah

@Composable
fun DestinationDetailsSelectorCard(
    calculatedTariff: Long,
    pickupAddress: String,
    destinationAddress: String,
    isCarSelected: Boolean,
    activeAddress: Boolean,
    availableSlots: Int,
    maxSlots: Int,
    isSlotSelectorEnabled: Boolean,
    onSlotsChanged: (Int) -> Unit,
    onSizeChange: (Dp) -> Unit = {},
    onSelectedVehicleChange: (Boolean) -> Unit = {},
    onActionButtonClick: () -> Unit = {},
    onActiveAddressChange: (Boolean) -> Unit = {},
    tripButtonAction: TripAction,
    isActionButtonEnabled: Boolean
) {
    val localDensity = LocalDensity.current

    Card(
        shape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp),
        modifier = Modifier.onGloballyPositioned {
            onSizeChange(with(localDensity) { it.size.height.toDp() })
        },
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(calculatedTariff.formatRupiah().isNotEmpty()) {
                Text(
                    text = calculatedTariff.formatRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(tripButtonAction != TripAction.CALCULATE_ROUTE_DRIVER && tripButtonAction != TripAction.DO_DRIVER_TRIP && tripButtonAction != TripAction.STOP_DRIVER_TRIP && tripButtonAction != TripAction.CALCULATING) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    VehicleTypeCard(
                        painter = painterResource(id = R.drawable.baseline_commute_24),
                        text = "Mobil",
                        modifier = Modifier.weight(1f),
                        isSelected = isCarSelected,
                        isEnabled = false,
                        onClick = {
                            onSelectedVehicleChange(true)
                        })
                    VehicleTypeCard(
                        painter = painterResource(id = R.drawable.baseline_two_wheeler_24),
                        text = "Motor",
                        modifier = Modifier.weight(1f),
                        isSelected = !isCarSelected,
                        isEnabled = true,
                        onClick = { onSelectedVehicleChange(false) })
                }
            }

            AnimatedVisibility(tripButtonAction != TripAction.CALCULATE_ROUTE_DRIVER && tripButtonAction != TripAction.DO_DRIVER_TRIP && tripButtonAction != TripAction.STOP_DRIVER_TRIP && tripButtonAction != TripAction.CALCULATING) {
                AddressCard(
                    content = pickupAddress,
                    placeholder = "Masukkan alamat penjemputan",
                    icon = painterResource(id = R.drawable.baseline_share_location_24),
                    isSelected = activeAddress,
                    onClick = {
                        onActiveAddressChange(true)
                    }
                )
            }

            AnimatedVisibility(tripButtonAction != TripAction.STOP_DRIVER_TRIP) {
                AddressCard(
                    content = destinationAddress,
                    placeholder = "Masukkan alamat tujuan",
                    icon = painterResource(id = R.drawable.baseline_location_on_24),
                    isSelected = !activeAddress,
                    onClick = {
                        onActiveAddressChange(false)
                    }
                )
            }

            AnimatedVisibility(visible = tripButtonAction == TripAction.DO_DRIVER_TRIP || tripButtonAction == TripAction.STOP_DRIVER_TRIP) {
                SlotSelector(
                    title = "Kursi tersedia untuk penumpang",
                    value = availableSlots,
                    onValueChange = onSlotsChanged,
                    range = 1..maxSlots,
                    isEnabled = isSlotSelectorEnabled
                )
            }


            TripActionButton(onActionButtonClick, tripButtonAction, isActionButtonEnabled)
        }
    }
}

@Composable
fun SlotSelector(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { onValueChange(value - 1) },
                enabled = isEnabled && value > range.first
            ) {
                Icon(painterResource(R.drawable.baseline_remove_circle_outline_24), "Kurangi")
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onValueChange(value + 1) },
                enabled = isEnabled && value < range.last
            ) {
                Icon(painterResource(R.drawable.baseline_add_circle_outline_24), "Tambah")
            }
        }
    }
}


@Composable
fun MatchingCard(
    modifier: Modifier = Modifier,
    isDriver: Boolean,
    onCancel: () -> Unit = {},
    onSizeChange: (Dp) -> Unit,
) {
    val localDensity = LocalDensity.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                onSizeChange(with(localDensity) { it.size.height.toDp() })
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isDriver) "Menunggu penumpang yang cocok..." else "Menunggu pengemudi yang cocok...",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Batalkan Pencarian")
            }
        }
    }
}

@Preview
@Composable
fun MatchingCardPassengerPreview() {
    PreviewBoilerplate {
        MatchingCard(
            isDriver = false,
            onCancel = {},
            onSizeChange = {}
        )
    }
}

@Preview
@Composable
fun MatchingCardDriverPreview() {
    PreviewBoilerplate {
        MatchingCard(
            isDriver = true,
            onCancel = {},
            onSizeChange = {}
        )
    }
}