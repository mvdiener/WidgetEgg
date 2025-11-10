package com.widgetegg.widgeteggapp.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.Routes
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import com.widgetegg.widgeteggapp.settings.settingsRowModifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun Stats(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateShowCommunityBadges(preferences.getShowCommunityBadges())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsHeader(
            text = "Back to Settings Screen",
            onClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Stats Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
            CommunityBadgesRow(settingsViewModel)
            StatsLegendRow(navController)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun CommunityBadgesRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Show community badges")
        }

        val scope = rememberCoroutineScope()
        Switch(
            checked = settingsViewModel.showCommunityBadges,
            onCheckedChange = {
                scope.launch {
                    settingsViewModel.updateShowCommunityBadges(!settingsViewModel.showCommunityBadges)
                }
            }
        )
    }
}

@Composable
fun StatsLegendRow(navController: NavController) {
    Row(
        modifier = Modifier
            .settingsRowModifier()
            .clickable { navController.navigate(Routes.statsLegendScreen) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Stats Legend")
        }

        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Stats Legend"
        )
    }
}