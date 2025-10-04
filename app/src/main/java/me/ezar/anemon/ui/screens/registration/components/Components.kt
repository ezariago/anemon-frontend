package me.ezar.anemon.ui.screens.registration.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ezar.anemon.R
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.ui.utils.PreviewBoilerplate

@Composable
fun AccountTypeCard(title: String, desc: String, painter: Painter, onClick: () -> Unit = {}) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun VehicleVerificationCard(
    onClick: () -> Unit,
    isCompleted: Boolean = false
) {
    val containerColor =
        if (isCompleted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor =
        if (isCompleted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val icon =
        if (isCompleted) painterResource(R.drawable.baseline_check_circle_24) else painterResource(R.drawable.baseline_camera_alt_24)
    val text = if (isCompleted) "Foto berhasil diambil!" else "Ambil foto kendaraanmu"
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .height(30.dp),
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
            topEnd = 0.dp,
            topStart = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Icon(
                icon, null, modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .padding(end = 8.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

@Composable
@Preview
fun AccountTypeCardPreview() {
    PreviewBoilerplate {
        AccountTypeCard(
            title = AccountVehiclePreference.PASSENGER.friendlyName,
            desc = AccountVehiclePreference.PASSENGER.desc,
            painter = painterResource(AccountVehiclePreference.PASSENGER.icon)
        )
    }
}

@Composable
@Preview
fun VehicleVerificationCardPreview() {
    PreviewBoilerplate {
        Column {
            VehicleVerificationCard(
                onClick = {},
            )
            VehicleVerificationCard(
                onClick = {}, isCompleted = true
            )
        }
    }
}