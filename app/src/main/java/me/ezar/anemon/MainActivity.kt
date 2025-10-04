package me.ezar.anemon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.ezar.anemon.navigation.AppNavigator
import me.ezar.anemon.service.NotificationHelper
import me.ezar.anemon.session.LocalStorage
import me.ezar.anemon.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        try {
            LocalStorage.initialize(getSharedPreferences("session", MODE_PRIVATE))
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Data yang ada sebelumnya sepertinya corrupt, coba login ulang ya!",
                Toast.LENGTH_LONG
            ).show()
            LocalStorage.reinitialize()
        }

        cacheDir.listFiles()?.forEach {
            it.delete()
        }

        setContent {
            AppContent()
        }
    }

    @Composable
    fun AppContent() {
        AppTheme {
            AppNavigator()
        }
    }

    @Preview
    @Composable
    fun AppPreview() {
        AppContent()
    }
}