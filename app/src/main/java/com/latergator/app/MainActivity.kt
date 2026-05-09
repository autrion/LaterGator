package com.latergator.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latergator.app.data.ReminderDatabase
import com.latergator.app.data.ReminderRepository
import com.latergator.app.ui.*
import com.latergator.app.ui.theme.LaterGatorTheme
import com.latergator.app.ui.theme.shouldUseDark
import kotlinx.coroutines.delay
import java.util.Calendar

private enum class Screen { MAIN, SETTINGS, HISTORY }

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Nutzer-Entscheidung wird respektiert */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()

        setContent {
            val app = application as LaterGatorApp
            val settingsRepo = app.settingsRepository
            val db = ReminderDatabase.getInstance(applicationContext)
            val reminderRepo = ReminderRepository(db.reminderDao())

            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application, settingsRepo)
            )
            val reminderVm: ReminderViewModel = viewModel(
                factory = ReminderViewModelFactory(application, reminderRepo, settingsRepo)
            )

            val settings by settingsVm.settings.collectAsState()
            val isSystemDark = isSystemInDarkTheme()

            var currentMinuteOfDay by remember { mutableIntStateOf(minuteOfDay()) }
            LaunchedEffect(Unit) {
                while (true) {
                    val msUntilNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
                    delay(msUntilNextMinute)
                    currentMinuteOfDay = minuteOfDay()
                }
            }

            val isDark = shouldUseDark(settings, isSystemDark, currentMinuteOfDay)
            var currentScreen by remember { mutableStateOf(Screen.MAIN) }

            LaterGatorTheme(darkTheme = isDark) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.MAIN -> MainScreen(
                            viewModel = reminderVm,
                            onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            onNavigateToHistory = { currentScreen = Screen.HISTORY },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            viewModel = settingsVm,
                            onBack = { currentScreen = Screen.MAIN }
                        )
                        Screen.HISTORY -> HistoryScreen(
                            viewModel = reminderVm,
                            onBack = { currentScreen = Screen.MAIN }
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun minuteOfDay(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}
