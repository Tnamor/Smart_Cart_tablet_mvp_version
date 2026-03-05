package com.smartcart

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.smartcart.navigation.AppNavigation
import com.smartcart.ui.theme.Background
import com.smartcart.ui.theme.SmartCartTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        startLockTask()   // Kiosk mode

        setContent {
            SmartCartTheme {
                Surface(Modifier.fillMaxSize(), color = Background) {
                    AppNavigation()
                }
            }
        }
    }

    @Deprecated("Blocked for kiosk")
    override fun onBackPressed() { /* intentionally empty */ }
}
