package me.ezar.anemon.ui.screens.trip.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class TripAction {
    CALCULATE_ROUTE_PASSENGER,
    CALCULATING,
    SEARCH_PASSENGER,

    CALCULATE_ROUTE_DRIVER,
    DO_DRIVER_TRIP,
    STOP_DRIVER_TRIP
}

@Composable
fun TripActionButton(onClick: () -> Unit = {}, action: TripAction, isEnabled: Boolean) {
    val animatedColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
        label = "color"
    )
    val colors =
        CardDefaults.elevatedCardColors().copy(
            containerColor = animatedColor,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        onClick = if (isEnabled) {
            onClick
        } else {
            {}
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (action) {
                    TripAction.CALCULATE_ROUTE_PASSENGER, TripAction.CALCULATE_ROUTE_DRIVER -> "Hitung rute"
                    TripAction.CALCULATING -> "Menghitung..."
                    TripAction.SEARCH_PASSENGER -> "Cari Pengemudi"
                    TripAction.DO_DRIVER_TRIP -> "Buat Perjalanan"
                    TripAction.STOP_DRIVER_TRIP -> "Hentikan Perjalanan"
                },
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AddressCard(
    content: String,
    placeholder: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "color"
    )
    val colors =
        CardDefaults.elevatedCardColors().copy(
            containerColor = animatedColor,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .height(24.dp)
                    .align(Alignment.CenterVertically),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = content.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (content.isEmpty()) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                    alpha = 0.65f
                ) else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VehicleTypeCard(
    painter: Painter,
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit = {}
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "color"
    )
    val colors = if (isEnabled)
        CardDefaults.elevatedCardColors().copy(
            containerColor = animatedColor,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    else
        CardDefaults.elevatedCardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth(),
        colors = colors,
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically),
                colorFilter = ColorFilter.tint(colors.contentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.contentColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

        }
    }
}