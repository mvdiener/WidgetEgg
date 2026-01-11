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
fun Contracts(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateUseAbsoluteTimeContract(preferences.getUseAbsoluteTimeContract())
        settingsViewModel.updateUseOfflineTime(preferences.getUseOfflineTime())
        settingsViewModel.updateOpenWasmeggDashboard(preferences.getOpenWasmeggDashboard())
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
            Text(text = "Contract Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
            ContractsGeneralGroup(settingsViewModel)
            LargeContractWidgetGroup(settingsViewModel)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun ContractsGeneralGroup(
    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Contracts General", fontSize = TextUnit(18f, TextUnitType.Sp))
        AbsoluteTimeContractRow(settingsViewModel)
        OfflineTimeRow(settingsViewModel)
        OpenWasmeggDashboardRow(settingsViewModel)
    }
}

@Composable
fun AbsoluteTimeContractRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show absolute time",
                "Show the estimated completion time instead of the estimated time remaining.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useAbsoluteTimeContract,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseAbsoluteTimeContract(!settingsViewModel.useAbsoluteTimeContract)
                    }
                }
            )
        }
    }
}

@Composable
fun OfflineTimeRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show offline data",
                "Show an estimated completion time and progress bars that include offline contribution.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useOfflineTime,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseOfflineTime(!settingsViewModel.useOfflineTime)
                    }
                }
            )
        }
    }
}

@Composable
fun OpenWasmeggDashboardRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Open eicoop dashboard",
                "Tapping any contract widget will open your eicoop dashboard, instead of manually refreshing all widgets. Uses Chrome if the default browser fails to open.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.openWasmeggDashboard,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateOpenWasmeggDashboard(!settingsViewModel.openWasmeggDashboard)
                    }
                }
            )
        }
    }
}

@Composable
fun LargeContractWidgetGroup(
    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Large Contract Widget", fontSize = TextUnit(18f, TextUnitType.Sp))
        ShowAvailableContractsRow(settingsViewModel)
    }
}

@Composable
fun ShowAvailableContractsRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show available contracts",
                "Vertically scroll through available contracts, in addition to active contracts.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.showAvailableContracts,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateShowAvailableContracts(!settingsViewModel.showAvailableContracts)
                    }
                }
            )
        }
    }
}