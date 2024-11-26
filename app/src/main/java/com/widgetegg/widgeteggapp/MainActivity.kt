package com.widgetegg.widgeteggapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.widgetegg.widgeteggapp.main.MainScreen
import com.widgetegg.widgeteggapp.settings.SettingsScreen
import com.widgetegg.widgeteggapp.ui.theme.WidgetEggTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetEggTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.mainScreen,
                        builder = {
                            composable(Routes.mainScreen) {
                                MainScreen(navController)
                            }
                            composable(Routes.settingsScreen) {
                                SettingsScreen(navController)
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "WidgetEgg" })
                }
            }
        }
    }
}


