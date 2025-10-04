package me.ezar.anemon.ui.screens.dashboard.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.ezar.anemon.ui.screens.dashboard.components.HostSelectionItem
import me.ezar.anemon.ui.utils.PreviewBoilerplate

@Composable
fun HostSelectionDialog(
    hosts: List<Pair<String, String>>, // Nama, Address
    onDismiss: () -> Unit = {},
    onHostSelected: (String) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pilih Server",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Ubah ini kalau peneliti nyuruh untuk ubah servernya ya",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hosts.size) { index ->
                        val (hostFriendlyName, hostname) = hosts[index]
                        HostSelectionItem(
                            hostFriendlyName = hostFriendlyName,
                            hostname = hostname,
                            onClick = {
                                onHostSelected(hostFriendlyName)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HostSelectionDialogPreview() {
    PreviewBoilerplate {
        HostSelectionDialog(
            hosts = listOf(
                "ANEMON 1" to "anemon.man1jember.sch.id",
                "ANEMON 2" to "anemon-prod.man1jember.sch.id",
                "ANEMON 3" to "anemon-dev.man1jember.sch.id"
            ),
            onHostSelected = { index -> println("Selected host at index: $index") }
        )
    }
}
