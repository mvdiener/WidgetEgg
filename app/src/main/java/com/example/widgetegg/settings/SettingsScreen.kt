package com.example.widgetegg.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.widgetegg.Routes
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun SettingsScreen(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current
    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateUseAbsoluteTime(preferences.getUseAbsoluteTime())
        settingsViewModel.updateShowTargetArtifactSmall(preferences.getTargetArtifactSmall())
        settingsViewModel.updateShowFuelingShip(preferences.getShowFuelingShip())
        settingsViewModel.updateOpenEggInc(preferences.getOpenEggInc())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 25.dp, top = 50.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            Icons.Rounded.Home,
            contentDescription = "Home",
            modifier = Modifier.clickable {
                navController.navigate(Routes.mainScreen)
            })
    }

    Column(
        modifier = Modifier
            .padding(top = 100.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Settings", fontSize = TextUnit(24f, TextUnitType.Sp))

        AbsoluteTimeRow(settingsViewModel)
        TargetArtifactRow(settingsViewModel)
        ShowFuelingShipRow(settingsViewModel)
        OpenEggIncRow(settingsViewModel)
    }
}

@Composable
fun AbsoluteTimeRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Show absolute time")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Absolute time info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowAbsoluteTimeDialog(true)
                    }
            )
            AbsoluteTimeDialog(settingsViewModel)
        }

        Switch(
            checked = settingsViewModel.useAbsoluteTime,
            onCheckedChange = {
                settingsViewModel.updateUseAbsoluteTime(!settingsViewModel.useAbsoluteTime)
            }
        )
    }
}

@Composable
fun AbsoluteTimeDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showAbsoluteTimeDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowAbsoluteTimeDialog(false)
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
                    text = "If a ship has less than 24 hours left, show the time of return instead of the time remaining."
                )
            }
        }
    }
}

@Composable
fun TargetArtifactRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Show target artifact")
        Switch(
            checked = settingsViewModel.showTargetArtifactSmall,
            onCheckedChange = {
                settingsViewModel.updateShowTargetArtifactSmall(!settingsViewModel.showTargetArtifactSmall)
            }
        )
    }
}

@Composable
fun ShowFuelingShipRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Show fueling ship")
        Switch(
            checked = settingsViewModel.showFuelingShip,
            onCheckedChange = {
                settingsViewModel.updateShowFuelingShip(!settingsViewModel.showFuelingShip)
            }
        )
    }
}

@Composable
fun OpenEggIncRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Open egg inc")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Open egg inc info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowOpenEggIncDialog(true)
                    }
            )
            OpenEggIncDialog(settingsViewModel)
        }

        Switch(
            checked = settingsViewModel.openEggInc,
            onCheckedChange = {
                settingsViewModel.updateOpenEggInc(!settingsViewModel.openEggInc)
            }
        )
    }
}

@Composable
fun OpenEggIncDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showOpenEggIncDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowOpenEggIncDialog(false)
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
Tapping the widget will open Egg, Inc. instead of manually refreshing the displayed missions.
                        
Automatic widget updates every 15 minutes will still happen independent of this setting.
                    """
                )
            }
        }
    }
}

private fun Modifier.settingsRowModifier() =
    this
        .padding(top = 50.dp, bottom = 10.dp, start = 25.dp, end = 25.dp)
        .fillMaxWidth()
