package com.mrsuffix.remindly

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
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.mrsuffix.remindly.domain.model.ThemeMode
import com.mrsuffix.remindly.domain.repository.SettingsRepository
import com.mrsuffix.remindly.notification.ReminderWorker
import com.mrsuffix.remindly.presentation.navigation.RemindlyNavGraph
import com.mrsuffix.remindly.ui.theme.RemindlyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleReminders()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestNotificationPermission()
        scheduleReminders()
        
        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            RemindlyTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    RemindlyNavGraph(navController = navController)
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    private fun scheduleReminders() {
        ReminderWorker.schedule(this)
    }
}