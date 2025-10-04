package me.ezar.anemon.session

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import me.ezar.anemon.models.requests.UserProfile

object LocalStorage {

    private var sharedPrefs: SharedPreferences? = null
    var cachedUserProfile: UserProfile? = null
        set(value) {
            field = value
            sharedPrefs?.edit {
                putString("cachedUserProfile", Json.encodeToString(value))
            }
        }

    var token: String = ""
        set(value) {
            field = value
            sharedPrefs?.edit {
                putString("token", value)
            }
        }

    var selectedServer: String = ""
        set(value) {
            field = value
            sharedPrefs?.edit {
                putString("selectedServer", value)
            }
        }

    fun reinitialize() {
        token = ""
        cachedUserProfile = null
    }

    fun initialize(prefs: SharedPreferences) {
        sharedPrefs = prefs
        token = prefs.getString("token", "") ?: ""
        selectedServer = prefs.getString("selectedServer", "MAN 1 Jember")
            ?: "MAN 1 Jember"
        prefs.getString("cachedUserProfile", null)?.let {
            cachedUserProfile = try {
                Json.decodeFromString(it)
            } catch (e: Exception) {
                throw (e) // buat handle klo cachenya error, semisal habis update
            }
        }
    }

    fun logout() {
        sharedPrefs?.edit {
            clear()
        }
        token = ""
        cachedUserProfile = null
    }
}