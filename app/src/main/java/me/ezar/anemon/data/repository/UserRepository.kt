package me.ezar.anemon.data.repository

import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import me.ezar.anemon.models.requests.LoginResponse
import me.ezar.anemon.models.requests.UserCreateRequest
import me.ezar.anemon.models.requests.UserStateResponse
import me.ezar.anemon.network.NetworkHandler
import me.ezar.anemon.network.services.UserService
import me.ezar.anemon.session.LocalStorage
import org.json.JSONObject

class UserRepository(
    private val userService: UserService = NetworkHandler.userService
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        val response = userService.loginUser(email, password)
        when (response.status) {
            HttpStatusCode.OK -> {
                return try {
                    LocalStorage.token = response.body<LoginResponse>().token
                    LocalStorage.cachedUserProfile = response.body<LoginResponse>().profile
                    Result.success(response)
                    Result.success(response.body())
                } catch (_: Exception) {
                    return Result.failure(Exception("Data status user tidak valid, hubungi admin untuk menyelesaikan masalah ini"))
                }
            }

            HttpStatusCode.Unauthorized -> {
                return Result.failure(Exception("Username atau password salah, silakan coba lagi"))
            }

            HttpStatusCode.UpgradeRequired -> {
                return Result.failure(Exception("Versi aplikasi sudah usang, silakan perbarui untuk melanjutkan"))
            }

            else -> {
                return Result.failure(Exception("Gagal mendapatkan status user: ${response.status}"))
            }
        }


    }

    suspend fun createUser(dto: UserCreateRequest): Result<Unit> {
        return try {
            val response = userService.createUser(dto)
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = try {
                    JSONObject(errorBody).getString("message")
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserState(): Result<UserStateResponse> {
        val state = userService.getUserState()
        when (state.status) {
            HttpStatusCode.OK -> {
                return try {
                    Result.success(state.body())
                } catch (_: Exception) {
                    return Result.failure(Exception("Data status user tidak valid, hubungi admin untuk menyelesaikan masalah ini"))
                }
            }

            HttpStatusCode.Unauthorized -> {
                return Result.failure(Exception("Kamu telah login di perangkat lain, mohon login ulang untuk melanjutkan"))
            }

            HttpStatusCode.UpgradeRequired -> {
                return Result.failure(Exception("Versi aplikasi sudah usang, silakan perbarui untuk melanjutkan"))
            }

            else -> {
                return Result.failure(Exception("Gagal mendapatkan status user: ${state.status}"))
            }
        }
    }
}