package me.ezar.anemon.models.enum

import me.ezar.anemon.R

enum class AccountVehiclePreference(
    val friendlyName: String,
    val icon: Int,
    val desc: String,
) {
    PASSENGER(
        "Penumpang Aja",
        icon = R.drawable.baseline_hail_24,
        "Gasuka nyetir, tapi pengen ikut selametin bumi"
    ),
    CAR(
        "Pengemudi Mobil",
        R.drawable.baseline_directions_car_24,
        "Bawa keluarga atau teman, bisa muat banyak barang"
    ),
    MOTORCYCLE(
        "Pengemudi Motor",
        R.drawable.baseline_two_wheeler_24,
        "Ringkes dan satset, enak dibawa kemana-mana"
    ),
}