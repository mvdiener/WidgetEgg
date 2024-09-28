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
import androidx.compose.material3.HorizontalDivider
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
import com.example.widgetegg.main.SignInViewModel
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun SettingsScreen(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current
    runBlocking {
        val preferences = PreferencesDatastore(context)
        val thing = preferences.getUseAbsoluteTime()
        settingsViewModel.updateUseAbsoluteTime(thing)
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
                    val updated = !settingsViewModel.useAbsoluteTime
                    settingsViewModel.updateUseAbsoluteTime(updated)
                }
            )
        }
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

private fun Modifier.settingsRowModifier() =
    this
        .padding(top = 50.dp, bottom = 10.dp, start = 25.dp, end = 25.dp)
        .fillMaxWidth()
