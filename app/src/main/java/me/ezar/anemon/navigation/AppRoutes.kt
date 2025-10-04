package me.ezar.anemon.navigation

sealed class AppRoutes(val route: String) {
    object Dashboard : AppRoutes("dashboard")
    object Login : AppRoutes("login")
    object Map : AppRoutes("map")

    object RegistrationFlow : AppRoutes("registration_flow") {
        object Register : AppRoutes("register")
        object VehiclePicture : AppRoutes("vehicle_picture")
        object Biodata : AppRoutes("biodata")
    }
}