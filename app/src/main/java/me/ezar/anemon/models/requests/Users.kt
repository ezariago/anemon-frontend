package me.ezar.anemon.models.requests

import kotlinx.serialization.Serializable
import me.ezar.anemon.models.enum.AccountVehiclePreference

// --- DTO client -> server ---

@Serializable
data class UserCreateRequest(
    val name: String,
    val email: String,
    val nik: String,
    val password: String,
    val vehiclePreference: AccountVehiclePreference,
    val profilePictureEncoded: String,
    val vehicleImageEncoded: String?
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// DTO server -> client ---

@Serializable
data class UserProfile(
    val uid: Int,
    val name: String,
    val email: String,
    val nik: String,
    val profilePictureId: String,
    val vehicleImageId: String,
    val vehiclePreference: AccountVehiclePreference,
) {
    companion object {
        fun mock1() = UserProfile(
            uid = -1,
            name = "John Doe",
            email = "johndoe@gmail.com",
            nik = "1234567890",
            profilePictureId = "profile1",
            vehicleImageId = "1",
            vehiclePreference = AccountVehiclePreference.PASSENGER,
        )

        fun mock2() = UserProfile(
            uid = -2,
            name = "Jane Smith",
            email = "janesmith@gmail.com",
            nik = "0987654321",
            profilePictureId = "profile2",
            vehicleImageId = "2",
            vehiclePreference = AccountVehiclePreference.MOTORCYCLE,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProfile) return false

        if (uid != other.uid) return false
        if (name != other.name) return false
        if (email != other.email) return false
        if (nik != other.nik) return false
        if (profilePictureId != other.profilePictureId) return false
        if (vehiclePreference != other.vehiclePreference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result *= 31 * uid.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + nik.hashCode()
        result = 31 * result + profilePictureId.hashCode()
        result = 31 * result + vehiclePreference.hashCode()
        return result
    }
}

@Serializable
data class LoginResponse(
    val token: String,
    val profile: UserProfile
)