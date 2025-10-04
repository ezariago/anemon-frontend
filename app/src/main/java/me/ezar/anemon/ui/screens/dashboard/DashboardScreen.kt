package me.ezar.anemon.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ezar.anemon.R
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.screens.dashboard.dialogs.HostSelectionDialog
import me.ezar.anemon.ui.utils.PreviewBoilerplate


@Composable
fun DashboardScreen(
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    contentPadding: PaddingValues
) {
    var selectedServer by remember { mutableStateOf(LocalStorage.selectedServer) }
    var showServerChangeDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(Modifier.padding(20.dp)) {
                    Image(
                        painterResource(R.drawable.anemon),
                        null,
                        modifier = Modifier.height(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                AssistChip(
                    onClick = {
                        showServerChangeDialog = true
                    },
                    colors = AssistChipDefaults.assistChipColors().copy(
                        //containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.primary,
                    ),
                    label = {
                        Text(text = "Server: $selectedServer")
                    },
                    leadingIcon = {
                        Image(
                            painterResource(R.drawable.baseline_travel_explore_24),
                            null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .scale(0.8f)
                )
            }
        },
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text(text = "Masuk")
                }
                OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text(text = "Belum punya akun? Daftar dulu")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 14.sp,
                    text = buildAnnotatedString {
                        val textStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )

                        val linkStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )

                        withStyle(textStyle) {
                            append("Aplikasi ini adalah percobaan penelitian. Jadi, pastiin kamu udah baca ")
                        }

                        withLink(
                            LinkAnnotation.Url( // Mungkin nanti diganti biar ga hardcode
                                "https://drive.google.com/file/d/10bu69NJ0UMtreWeYzrmWS9xgb3bzVxPz/view?usp=sharing",
                                TextLinkStyles(style = linkStyle)
                            )
                        ) {
                            append("informed consent")
                        }

                        withStyle(textStyle) {
                            append(" yang berlaku di sini yaa.")
                        }
                    })

            }
        }
    ) {
        if (showServerChangeDialog) {
            HostSelectionDialog(
                hosts = NetworkHandler.hosts.map { it.key to it.value },
                onDismiss = { showServerChangeDialog = false },
                onHostSelected = { host ->
                    LocalStorage.selectedServer = host
                    selectedServer = host
                    showServerChangeDialog = false
                }
            )
        }
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            val pagerState = rememberPagerState(pageCount = {
                3
            })
            HorizontalPager(state = pagerState) { page ->
                Image(
                    when (page) {
                        0 -> {
                            painterResource(R.drawable.welcome1)
                        }

                        1 -> {
                            painterResource(R.drawable.welcome2)
                        }

                        2 -> {
                            painterResource(R.drawable.welcome3)
                        }

                        else -> {
                            throw IllegalStateException("No image for page $page")
                        }
                    },
                    null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            Row {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DashboardPreview() {
    PreviewBoilerplate {
        DashboardScreen(contentPadding = PaddingValues(0.dp))
    }
}