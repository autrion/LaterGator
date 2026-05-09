package com.latergator.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latergator.app.data.ReminderDatabase
import com.latergator.app.data.ReminderRepository
import com.latergator.app.ui.MainScreen
import com.latergator.app.ui.ReminderViewModel
import com.latergator.app.ui.ReminderViewModelFactory
import com.latergator.app.ui.theme.LaterGatorTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Benachrichtigungen werden nach Möglichkeit gezeigt */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            LaterGatorTheme {
                val db = ReminderDatabase.getInstance(applicationContext)
                val repo = ReminderRepository(db.reminderDao())
                val vm: ReminderViewModel = viewModel(
                    factory = ReminderViewModelFactory(application, repo)
                )
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = vm,
                        modifier = Modifier.padding(innerPadding)
                    )
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
