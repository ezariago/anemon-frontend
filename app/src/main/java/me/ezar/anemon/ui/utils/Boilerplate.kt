package me.ezar.anemon.ui.utils

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import me.ezar.anemon.network.NetworkHandler.hosts
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.theme.AppTheme

@Composable
fun PreviewBoilerplate(
    content: @Composable () -> Unit
) {
    AppTheme {
        content()
    }
}

@Composable
fun SimpleTopBar(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        content()
    }
}

@Composable
fun Center(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}

fun profilePictureBuilder(context: Context, profileId: String): ImageRequest {
    return ImageRequest.Builder(context)
        .data("https://${hosts[LocalStorage.selectedServer]}/images/$profileId")
        .httpHeaders(
            NetworkHeaders.Builder()
                .add("Authorization", "Bearer ${LocalStorage.token}")
                .build()
        )
        .build()
}