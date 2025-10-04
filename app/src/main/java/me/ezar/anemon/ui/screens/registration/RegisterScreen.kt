package me.ezar.anemon.ui.screens.registration

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ezar.anemon.R
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.ui.screens.registration.components.AccountTypeCard
import me.ezar.anemon.ui.screens.registration.components.VehicleVerificationCard
import me.ezar.anemon.ui.screens.registration.dialogs.AccountTypeDialog
import me.ezar.anemon.ui.screens.registration.dialogs.VehiclePicturePreviewDialog
import me.ezar.anemon.ui.screens.registration.dialogs.VehiclePreferenceDialog
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.SimpleTopBar
import java.time.LocalDateTime

@Composable
fun RegisterScreen(
    registrationViewModel: RegistrationViewModel,
    onNextButton: () -> Unit = {},
    onBackButton: () -> Unit = {},
    onSelectVehicleButton: () -> Unit = {},
    contentPadding: PaddingValues
) {
    val scrollState = rememberScrollState()

    var showAccountTypeDialog by remember { mutableStateOf(false) }
    var showVehiclePreferenceDialog by remember { mutableStateOf(false) }
    var showVehiclePicturePreviewDialog by remember { mutableStateOf(false) }

    val emailError = !("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        .matches(registrationViewModel.email)) && registrationViewModel.email.isNotBlank()

    val passwordHasUppercase = "[A-Z]".toRegex().containsMatchIn(registrationViewModel.password)
    val passwordHasNumber = "[1-9]".toRegex().containsMatchIn(registrationViewModel.password)
    val passwordRequirementMet = passwordHasUppercase && passwordHasNumber

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
                        "Daftar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        bottomBar = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Button(modifier = Modifier.fillMaxWidth(0.9f), onClick = {
                        if (!(emailError || !passwordRequirementMet || registrationViewModel.email.isBlank() || registrationViewModel.password.isBlank()))
                            if (registrationViewModel.accountType == AccountVehiclePreference.PASSENGER) {
                                onNextButton()
                            } else {
                                if (registrationViewModel.vehicleImageBytes == null) {
                                    return@Button
                                } else {
                                    showVehiclePicturePreviewDialog = true
                                }
                            }
                        onNextButton()
                    }) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    ) {
        if (showAccountTypeDialog) {
            AccountTypeDialog(
                onClick = {
                    showAccountTypeDialog = false
                    val type = it ?: run {
                        showVehiclePreferenceDialog = true
                        return@AccountTypeDialog
                    }
                    registrationViewModel.accountType = type
                    registrationViewModel.vehicleImageBytes = null
                },
                onDismiss = {
                    showAccountTypeDialog = false
                }
            )
        }

        if (showVehiclePreferenceDialog) {
            VehiclePreferenceDialog(
                onClick = {
                    showVehiclePreferenceDialog = false
                    registrationViewModel.accountType = it
                    registrationViewModel.vehicleImageBytes = null
                },
                onDismiss = {
                    showVehiclePreferenceDialog = false
                }
            )
        }

        if (showVehiclePicturePreviewDialog) {
            registrationViewModel.vehicleImageBytes?.let {
                VehiclePicturePreviewDialog(
                    imageBytes = it,
                    onDismiss = { showVehiclePicturePreviewDialog = false },
                    onDelete = {
                        registrationViewModel.vehicleImageBytes = null
                        showVehiclePicturePreviewDialog = false
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(it)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(scrollState)
        ) {
            val currentTime = LocalDateTime.now().hour
            val parsedTime =
                if (currentTime > 18) "malemm"
                else if (currentTime > 15) "soree"
                else if (currentTime > 9) "siangg"
                else "pagii"

            Text(
                "Met $parsedTime! Daftar dulu yaa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Makasihh udah mau berpartisipasi dalam penelitian kami 😁🙏 ",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.SansSerif,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = buildAnnotatedString {
                val style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                )
                withStyle(style = style) {
                    append("Tipe Akun")
                }
            })

            Spacer(modifier = Modifier.height(8.dp))
            AccountTypeCard(
                title = registrationViewModel.accountType.friendlyName,
                desc = registrationViewModel.accountType.desc,
                painter = painterResource(registrationViewModel.accountType.icon),
                onClick = {
                    showAccountTypeDialog = true
                }
            )
            AnimatedVisibility(registrationViewModel.accountType != AccountVehiclePreference.PASSENGER) {
                VehicleVerificationCard(
                    onClick = {
                        if (registrationViewModel.vehicleImageBytes != null) {
                            showVehiclePicturePreviewDialog = true
                        } else {
                            onSelectVehicleButton()
                        }
                    },
                    isCompleted = registrationViewModel.vehicleImageBytes != null
                )
            }
            Spacer(Modifier.height(16.dp))

            val regularTextStyle = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
            )

            Text(buildAnnotatedString {
                withStyle(regularTextStyle) {
                    append("Alamat Email ")
                }
            })

            TextField(
                value = registrationViewModel.email,
                isError = emailError,
                onValueChange = {
                    registrationViewModel.email = it
                },
                placeholder = { Text("anemon@gmail.com") },
                modifier = Modifier
                    .fillMaxWidth(),
                supportingText = {
                    AnimatedVisibility(emailError) {
                        Text("Masukin email yang bener ya!")
                    }
                },
                colors = TextFieldDefaults.colors().copy(
                    unfocusedContainerColor = Color.Transparent,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(buildAnnotatedString {
                withStyle(regularTextStyle) {
                    append("Kata Sandi ")
                }
            })
            TextField(
                value = registrationViewModel.password,
                isError = !passwordRequirementMet && registrationViewModel.password.isNotBlank(),
                onValueChange = { registrationViewModel.password = it },
                placeholder = { Text("Buat sandi yang kuat, ya!") },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors().copy(
                    unfocusedContainerColor = Color.Transparent,
                ),
                supportingText = {
                    AnimatedVisibility(registrationViewModel.password.isNotBlank()) {
                        Column {
                            AnimatedVisibility(!passwordHasNumber) {
                                Text("Tambahkan angka dalam passwordmu")
                            }
                            AnimatedVisibility(!passwordHasUppercase) {
                                Text("Tambahkan huruf kapital dalam passwordmu")
                            }
                        }
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun RegisterScreenPreview() {
    PreviewBoilerplate {
        RegisterScreen(
            registrationViewModel = RegistrationViewModel(UserRepository()),
            contentPadding = PaddingValues(0.dp)
        )
    }
}