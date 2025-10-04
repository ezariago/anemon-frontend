package me.ezar.anemon.ui.screens.registration.dialogs

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.ezar.anemon.R
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.ui.utils.PreviewBoilerplate

@Composable
fun AccountTypeDialog(
    onClick: (AccountVehiclePreference?) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Biasanya kamu kemana-mana naik apa?",
                    style = MaterialTheme.typography.titleMedium
                )

                ElevatedCard(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                    onClick = {
                        onClick(null)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.baseline_commute_24),
                            null,
                            modifier = Modifier.fillMaxHeight(0.6f)
                        )
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Text(
                                "Nyetir sendiri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Tim yang gasuka ribet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                ElevatedCard(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                    onClick = {
                        onClick(AccountVehiclePreference.PASSENGER)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.baseline_hail_24),
                            null,
                            modifier = Modifier.fillMaxHeight(0.6f)
                        )
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Text(
                                "Ikut orang lain",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Tim yang suka naik angkot atau ojol",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Text(
                    "Jangan khawatir, kamu bisa ubah pilihan ini nanti.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun VehiclePreferenceDialog(
    onClick: (AccountVehiclePreference) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val ctx = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Kendaraannya apa?",
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 8.dp),
                        elevation = CardDefaults.elevatedCardElevation(2.dp),
                        onClick = {
                            //onClick(AccountVehiclePreference.CAR)
                            Toast.makeText(
                                ctx,
                                "Sepurane rekk, fitur iki gak iso digawe sek an 😔",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.baseline_directions_car_24),
                                null,
                                modifier = Modifier.fillMaxWidth(0.5f),
                                colorFilter = ColorFilter.tint(Color.Gray)
                            )
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Mobil",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 8.dp),
                        elevation = CardDefaults.elevatedCardElevation(2.dp),
                        onClick = {
                            onClick(AccountVehiclePreference.MOTORCYCLE)
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.baseline_two_wheeler_24),
                                null,
                                modifier = Modifier.fillMaxWidth(0.5f)
                            )
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Motor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Text(
                    "Jangan khawatir, kamu bisa ubah pilihan ini nanti.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview
@Composable
fun AccountTypeDialogPreview() {
    PreviewBoilerplate {
        AccountTypeDialog()
    }
}

@Preview
@Composable
fun VehiclePreferenceDialogPreview() {
    PreviewBoilerplate {
        VehiclePreferenceDialog()
    }
}