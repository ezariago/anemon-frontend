package me.ezar.anemon.ui.screens.login

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ezar.anemon.R
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.ui.utils.PreviewBoilerplate
import me.ezar.anemon.ui.utils.SimpleTopBar

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { result ->
            result.onSuccess {
                onLoginSuccess()
            }.onFailure {
                Toast.makeText(context, it.message ?: "Login Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    val emailError = !("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        .matches(viewModel.email)) && viewModel.email.isNotBlank()
    val passwordHasUppercase = "[A-Z]".toRegex().containsMatchIn(viewModel.password)
    val passwordHasNumber = "[1-9]".toRegex().containsMatchIn(viewModel.password)
    val passwordRequirementMet = passwordHasUppercase && passwordHasNumber
    val isFormValid =
        viewModel.email.isNotBlank() && viewModel.password.isNotBlank() && !emailError && passwordRequirementMet

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(contentPadding)) {
                SimpleTopBar {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Kembali"
                        )
                    }
                    Text(
                        "Masuk",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading && isFormValid,
                    onClick = { viewModel.onLoginClicked() }
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Masuk")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text("Alamat Email", fontWeight = FontWeight.SemiBold)
            TextField(
                value = viewModel.email,
                isError = emailError,
                onValueChange = { viewModel.email = it },
                placeholder = { Text("anemon@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (emailError) Text("Masukin email yang bener ya!")
                },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text("Kata Sandi", fontWeight = FontWeight.SemiBold)
            TextField(
                value = viewModel.password,
                isError = !passwordRequirementMet && viewModel.password.isNotBlank(),
                onValueChange = { viewModel.password = it },
                placeholder = { Text("Buat sandi yang kuat, ya!") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                supportingText = {
                    AnimatedVisibility(viewModel.password.isNotBlank() && !passwordRequirementMet) {
                        Column {
                            if (!passwordHasNumber) Text("Tambahkan angka dalam passwordmu")
                            if (!passwordHasUppercase) Text("Tambahkan huruf kapital dalam passwordmu")
                        }
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun LoginScreenPreview() {
    PreviewBoilerplate {
        val vm = LoginViewModel(UserRepository())
        LoginScreen(
            viewModel = vm,
            onLoginSuccess = {},
            onNavigateBack = {},
            contentPadding = PaddingValues()
        )
    }
}