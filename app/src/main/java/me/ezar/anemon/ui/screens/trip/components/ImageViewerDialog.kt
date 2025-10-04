package me.ezar.anemon.ui.screens.trip.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import me.ezar.anemon.R
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.session.LocalStorage

@Composable
fun ImageViewerDialog(
    imagePathId: String,
    isVehicleImage: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val fullUrl = "https://${NetworkHandler.hosts[LocalStorage.selectedServer]}/images/$imagePathId"

    val imageRequest = ImageRequest.Builder(context)
        .data(fullUrl)
        .httpHeaders(
            NetworkHeaders.Builder()
                .add("Authorization", "Bearer ${LocalStorage.token}")
                .build()
        )
        .build()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = if (isVehicleImage) "Foto Kendaraan" else "Foto Profil",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                error = painterResource(R.drawable.baseline_close_24),
                placeholder = painterResource(R.drawable.baseline_circle_24)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.baseline_close_24),
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}