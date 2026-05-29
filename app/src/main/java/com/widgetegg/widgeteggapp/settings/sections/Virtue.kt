package com.widgetegg.widgeteggapp.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsHeaderAndDescription
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import com.widgetegg.widgeteggapp.settings.settingsRowModifier
import com.widgetegg.widgeteggapp.settings.widgetGroupingModifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun Virtue(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateUseAbsoluteTimeVirtueSilos(preferences.getUseAbsoluteTimeVirtueSilos())
        settingsViewModel.updateUseAbsoluteTimeVirtueNextTruthEgg(preferences.getUseAbsoluteTimeVirtueNextTruthEgg())
        settingsViewModel.updateShowNextTruthEggGoalAmount(preferences.getShowNextTruthEggGoalAmount())
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
            Text(text = "Virtue Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
            VirtueGeneralGroup(settingsViewModel)
            LargeVirtueWidgetGroup(settingsViewModel)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun VirtueGeneralGroup(
    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Virtue General", fontSize = TextUnit(18f, TextUnitType.Sp))
        AbsoluteTimeVirtueSilosRow(settingsViewModel)
        AbsoluteTimeVirtueTruthEggRow(settingsViewModel)
    }
}

@Composable
fun AbsoluteTimeVirtueSilosRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show silos absolute time",
                "Show when silos will be empty instead of the silo time remaining.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useAbsoluteTimeVirtueSilos,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseAbsoluteTimeVirtueSilos(!settingsViewModel.useAbsoluteTimeVirtueSilos)
                    }
                }
            )
        }
    }
}

@Composable
fun AbsoluteTimeVirtueTruthEggRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show next TE absolute time",
                "Show the estimated TE goal time instead of the estimated time remaining.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useAbsoluteTimeVirtueNextTruthEgg,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseAbsoluteTimeVirtueNextTruthEgg(!settingsViewModel.useAbsoluteTimeVirtueNextTruthEgg)
                    }
                }
            )
        }
    }
}

@Composable
fun LargeVirtueWidgetGroup(settingsViewModel: SettingsViewModel) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Large Virtue Widget", fontSize = TextUnit(18f, TextUnitType.Sp))
        ShowTruthEggGoalRow(settingsViewModel)
    }
}

@Composable
fun ShowTruthEggGoalRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show next TE goal amount",
                null,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.showNextTruthEggGoalAmount,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateShowNextTruthEggGoalAmount(!settingsViewModel.showNextTruthEggGoalAmount)
                    }
                }
            )
        }
    }
}