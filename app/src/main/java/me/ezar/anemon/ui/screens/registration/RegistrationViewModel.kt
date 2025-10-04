package me.ezar.anemon.ui.screens.registration

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import me.ezar.anemon.data.repository.UserRepository
import me.ezar.anemon.models.enum.AccountVehiclePreference
import me.ezar.anemon.models.requests.UserCreateRequest

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {
    // Screen 1 State
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var accountType by mutableStateOf(AccountVehiclePreference.PASSENGER)
    var vehicleImageBytes: ByteArray? by mutableStateOf(null)

    // Screen 2 State
    var profilePictureBytes: ByteArray by mutableStateOf(byteArrayOf())
    var name by mutableStateOf("")
    var nik by mutableStateOf("")

    // Shared state
    var isLoading by mutableStateOf(false)
        private set

    private val _registrationResult = MutableSharedFlow<Result<Unit>>()
    val registrationResult = _registrationResult.asSharedFlow()


    fun onRegisterClicked() {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            val vehicleImageEncoded = if (accountType != AccountVehiclePreference.PASSENGER) {
                vehicleImageBytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
            } else null

            val profilePictureEncoded = Base64.encodeToString(profilePictureBytes, Base64.NO_WRAP)

            val request = UserCreateRequest(
                name = name,
                email = email,
                nik = nik,
                password = password,
                vehiclePreference = accountType,
                profilePictureEncoded = profilePictureEncoded,
                vehicleImageEncoded = vehicleImageEncoded,
            )

            val result = userRepository.createUser(request)
            _registrationResult.emit(result)
            isLoading = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RegistrationViewModel(
                    userRepository = UserRepository()
                )
            }
        }
    }
}