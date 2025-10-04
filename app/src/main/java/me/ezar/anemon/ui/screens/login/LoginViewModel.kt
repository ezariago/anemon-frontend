package me.ezar.anemon.ui.screens.login

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

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set

    private val _loginResult = MutableSharedFlow<Result<Unit>>()
    val loginResult = _loginResult.asSharedFlow()

    fun onLoginClicked() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            val result = userRepository.login(email, password)
            result.onSuccess {
                _loginResult.emit(Result.success(Unit))
            }.onFailure {
                _loginResult.emit(Result.failure(it))
            }
            isLoading = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    userRepository = UserRepository()
                )
            }
        }
    }
}