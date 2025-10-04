package me.ezar.anemon.ui.utils

import io.ktor.websocket.Frame
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun String.parseNIKToDateOfBirth(): String {
    if (this.length != 16) return "Invalid NIK"

    val day = this.substring(6, 8).toInt().let {
        if (it > 40) it - 40 else it // Kalau diatas 40 berarti cewek
    }
    val month = this.substring(8, 10).toInt()
    val year = this.substring(10, 12)

    val fullYear = if (year.toInt() < 50) "20${year}" else "19$year"

    return "$day ${month.toIndonesianMonth()} $fullYear"
}

fun String.isMale(): Boolean {
    return this.substring(6, 8).toInt() < 40
}

fun Int.toIndonesianMonth(): String {
    return when (this) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "Desember"
        else -> "Bulan tidak valid"
    }
}

fun Long.formatRupiah(): String {
    if (toString() == "-1") return ""
    return "Rp ${this.toString().reversed().chunked(3).joinToString(".").reversed()}"
}

fun String.toFrameText(): Frame.Text {
    return Frame.Text(
        this
    )
}

@OptIn(ExperimentalEncodingApi::class)
fun String.toBase64(): String {
    return Base64.encode(this.toByteArray())
}

@OptIn(ExperimentalEncodingApi::class)
fun String.fromBase64(): String {
    return Base64.decode(this).decodeToString()
}