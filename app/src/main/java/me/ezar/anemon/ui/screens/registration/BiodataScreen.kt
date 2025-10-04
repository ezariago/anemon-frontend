package me.ezar.anemon.ui.screens.registration

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import me.ezar.anemon.R
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.ui.screens.registration.dialogs.ProfilePicturePickerDialog
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.SimpleTopBar
import me.ezar.anemon.ui.utils.isMale
import me.ezar.anemon.ui.utils.parseNIKToDateOfBirth

@Composable
fun BiodataScreen(
    registrationViewModel: RegistrationViewModel,
    onBackButton: () -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    contentPadding: PaddingValues
) {
    var showProfileImagePickerDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isValidNIK =
        "^(1[1-9]|21|[37][1-6]|5[1-3]|6[1-5]|[89][12])\\d{2}\\d{2}([04][1-9]|[1256][0-9]|[37][01])(0[1-9]|1[0-2])\\d{2}\\d{4}$".toRegex()
            .matches(registrationViewModel.nik)
    val isFormValid = registrationViewModel.name.isNotBlank() && isValidNIK

    LaunchedEffect(Unit) {
        registrationViewModel.registrationResult.collectLatest { result ->
            result.onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "An unknown error occurred") }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(contentPadding)) {
                SimpleTopBar {
                    IconButton(onClick = onBackButton) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Kembali"
                        )
                    }
                    Text(
                        "Data diri",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                var termsChecked by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = termsChecked, onCheckedChange = { termsChecked = it })
                    Text(buildAnnotatedString {
                        append("Saya telah membaca dan menyetujui ")
                        withLink(
                            LinkAnnotation.Url( // Nanti ganti biar ga hardcode
                                "https://drive.google.com/file/d/10bu69NJ0UMtreWeYzrmWS9xgb3bzVxPz/view?usp=sharing",
                                TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append("informed consent")
                        }
                        append(" yang berlaku.")
                    }, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !registrationViewModel.isLoading && termsChecked,
                    onClick = { registrationViewModel.onRegisterClicked() }
                ) {
                    if (registrationViewModel.isLoading) {
                        Text("Sipp, tunggu sebentar yaa... ")
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Daftar")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Foto Profil", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .height(200.dp)
                        .width(200.dp)
                        .clickable(onClick = {
                            showProfileImagePickerDialog = true
                        }),
                    contentAlignment = Alignment.Center
                )
                {
                    if (registrationViewModel.profilePictureBytes.isEmpty()) {
                        Card {
                            Column(modifier = Modifier.fillMaxSize()) {
                            }
                        }
                        Icon(painterResource(R.drawable.baseline_camera_alt_24), null)
                    } else {
                        val bytes = registrationViewModel.profilePictureBytes
                        val bitmap = remember(bytes) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                        }

                        Image(
                            bitmap = bitmap,
                            contentDescription = "Preview foto kendaraan",

                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (showProfileImagePickerDialog) {
                ProfilePicturePickerDialog(
                    registrationViewModel,
                    onBackButton = { showProfileImagePickerDialog = false }) {
                    showProfileImagePickerDialog = false
                }
            }


            Spacer(Modifier.height(16.dp))
            Text("Nama Lengkap", fontWeight = FontWeight.SemiBold)
            TextField(
                value = registrationViewModel.name,
                onValueChange = { registrationViewModel.name = it },
                placeholder = { Text("Masukin sesuai KTP yaa") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
            )

            Spacer(Modifier.height(16.dp))

            Text("NIK", fontWeight = FontWeight.SemiBold)
            TextField(
                value = registrationViewModel.nik,
                onValueChange = { registrationViewModel.nik = it },
                placeholder = { Text("16 digit Nomor Induk Kependudukan") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
            )

            AnimatedVisibility(isValidNIK) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tanggal Lahir: ${registrationViewModel.nik.parseNIKToDateOfBirth()}")
                    Icon(
                        painter = painterResource(if (registrationViewModel.nik.isMale()) R.drawable.baseline_male_24 else R.drawable.baseline_female_24),
                        contentDescription = "Gender"
                    )
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun BiodataScreenPreview() {
    PreviewBoilerplate {
        val vm = RegistrationViewModel(UserRepository())
        BiodataScreen(
            registrationViewModel = vm,
            onBackButton = {},
            onSuccess = {},
            onError = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}