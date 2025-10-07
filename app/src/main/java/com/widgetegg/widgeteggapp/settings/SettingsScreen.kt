package com.widgetegg.widgeteggapp.settings

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.Routes

@Composable
fun SettingsScreen(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()
    val context = LocalContext.current

    val packageName = context.packageName
    val pm: PowerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    settingsViewModel.updateIsOptimizationDisabled(pm.isIgnoringBatteryOptimizations(packageName))

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.updateIsOptimizationDisabled(pm.isIgnoringBatteryOptimizations(packageName))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsHeader(
            text = "Back to Home Screen",
            onClick = { navController.navigate(Routes.mainScreen) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            item {
                if (!settingsViewModel.isOptimizationDisabled) {
                    BatteryPermissionsContent(settingsViewModel, packageName, context)
                }
            }
            item {
                SettingsSectionItem(
                    text = "General",
                    onClick = { navController.navigate(Routes.generalSettingsScreen) }
                )
            }
            item {
                SettingsSectionItem(
                    text = "Missions",
                    onClick = { navController.navigate(Routes.missionsSettingsScreen) }
                )
            }
            item {
                SettingsSectionItem(
                    text = "Contracts",
                    onClick = { navController.navigate(Routes.contractsSettingsScreen) }
                )
            }
            item {
                SettingsSectionItem(
                    text = "Stats",
                    onClick = { navController.navigate(Routes.statsSettingsScreen) }
                )
            }
        }
    }
}

@Composable
fun BatteryPermissionsContent(
    settingsViewModel: SettingsViewModel,
    packageName: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
            .clickable {
                settingsViewModel.updateShowBatteryOptimizationDialog(true)
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Warning,
            contentDescription = "Battery permissions warning",
            modifier = Modifier
                .padding(end = 5.dp)
                .size(35.dp),
            tint = Color(0xffffa500)
        )
        Text(text = "Battery Optimization Enabled")
        Icon(
            Icons.Rounded.Info,
            contentDescription = "Battery optimization info",
            modifier = Modifier
                .padding(start = 5.dp)
                .size(15.dp)
        )
        BatteryPermissionsDialog(settingsViewModel, packageName, context)
    }
}

@Composable
fun BatteryPermissionsDialog(
    settingsViewModel: SettingsViewModel,
    packageName: String,
    context: Context
) {
    if (settingsViewModel.showBatteryOptimizationDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowBatteryOptimizationDialog(false)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text =
                        """
                        The widgets attempt to update automatically in the background every 15 minutes. In order to do this, they run background processes to fetch mission data.     
                        
                        Battery optimization can prevent these processes from running. If you notice issues with widget updates, it is recommended that you turn off battery optimization for this app.
                    """.trimIndent()
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 5.dp),
                    onClick = {
                        settingsViewModel.updateShowBatteryOptimizationDialog(false)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = "package:$packageName".toUri()
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = "App Settings")
                }
            }
        }
    }
}