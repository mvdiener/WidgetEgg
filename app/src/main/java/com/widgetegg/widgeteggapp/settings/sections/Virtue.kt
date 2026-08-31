package com.widgetegg.widgeteggapp.settings.sections

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.MainActivity
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsHeaderAndDescription
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import com.widgetegg.widgeteggapp.settings.settingsRowModifier
import com.widgetegg.widgeteggapp.settings.widgetGroupingModifier
import data.constants.ALL_EVENT_TYPES
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tools.utilities.hasNotificationPermissions
import user.preferences.PreferencesDatastore

@Composable
fun Virtue(navController: NavController, activity: MainActivity) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateUseAbsoluteTimeVirtueSilos(preferences.getUseAbsoluteTimeVirtueSilos())
        settingsViewModel.updateUseAbsoluteTimeVirtueNextTruthEgg(preferences.getUseAbsoluteTimeVirtueNextTruthEgg())
        settingsViewModel.updateOpenVirtueCompanion(preferences.getOpenVirtueCompanion())
        settingsViewModel.updateShowNextTruthEggGoalAmount(preferences.getShowNextTruthEggGoalAmount())
        settingsViewModel.updateSelectedEvents(preferences.getSelectedEventNotifications())
        settingsViewModel.updateShowNextFiveGoals(preferences.getShowNextFiveGoals())
    }

    if (!hasNotificationPermissions(context)) {
        settingsViewModel.updateSelectedEvents(emptySet())
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val onResumeNotificationPermissions = hasNotificationPermissions(context)

        if (!onResumeNotificationPermissions) {
            settingsViewModel.updateSelectedEvents(emptySet())
        }
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
            VirtueGeneralGroup(settingsViewModel, context, activity)
            LargeVirtueWidgetGroup(settingsViewModel)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun VirtueGeneralGroup(
    settingsViewModel: SettingsViewModel,
    context: Context,
    activity: MainActivity
) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Virtue General", fontSize = TextUnit(18f, TextUnitType.Sp))
        AbsoluteTimeVirtueSilosRow(settingsViewModel)
        AbsoluteTimeVirtueTruthEggRow(settingsViewModel)
        OpenVirtueCompanionRow(settingsViewModel)
        ShowNextFiveGoalsRow(settingsViewModel)
        EventNotificationRow(settingsViewModel, context, activity)
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
fun OpenVirtueCompanionRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Open virtue companion",
                "Tapping any virtue widget will open the wasmegg virtue companion, instead of manually refreshing all widgets. Uses Chrome if the default browser fails to open.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.openVirtueCompanion,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateOpenVirtueCompanion(!settingsViewModel.openVirtueCompanion)
                    }
                }
            )
        }
    }
}

@Composable
fun ShowNextFiveGoalsRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show next five TE goals",
                "Show the next five TE goals for the current egg, instead of the next TE goal for all five virtue eggs. Only applies when on virtue.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.showNextFiveGoals,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateShowNextFiveGoals(!settingsViewModel.showNextFiveGoals)
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

@Composable
fun EventNotificationRow(
    settingsViewModel: SettingsViewModel,
    context: Context,
    activity: MainActivity
) {
    Row(
        modifier = Modifier
            .settingsRowModifier()
            .clickable {
                if (hasNotificationPermissions(context)) {
                    settingsViewModel.updateShowEventNotificationDialog(true)
                } else {
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ),
                        101
                    )
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val selectedDescription = if (settingsViewModel.selectedEvents.isNotEmpty()) {
                val names = ALL_EVENT_TYPES.filter { it.id in settingsViewModel.selectedEvents }
                    .map { it.displayName }
                "Selected: " + names.joinToString(separator = ", ")
            } else {
                "Selected: none"
            }
            SettingsHeaderAndDescription(
                "Notify on selected events",
                selectedDescription,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )

            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add event notifications"
            )
            if (settingsViewModel.showEventNotificationDialog) {
                EventNotificationDialog(settingsViewModel)
            }
        }
    }
}

@Composable
fun EventNotificationDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showEventNotificationDialog) {
        var tempSelection by remember { mutableStateOf(settingsViewModel.selectedEvents) }
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateSelectedEvents(tempSelection)
                settingsViewModel.updateShowEventNotificationDialog(false)
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
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Text(text = "Select Events", fontSize = TextUnit(18f, TextUnitType.Sp))
                        Text(
                            text =
                                """
                                    Timeliness of notifications not guaranteed. Notifications may arrive at any time after an event starts.
                                    
                                    Notifications for ultra events will only occur if you have ultra.
                                """.trimIndent(),
                            fontSize = TextUnit(13f, TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp),
                        onClick = {
                            tempSelection = ALL_EVENT_TYPES.map { it.id }.toSet()
                        }) {
                        Text(text = "Select All")
                    }
                    Button(
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp),
                        onClick = {
                            tempSelection = emptySet()
                        }) {
                        Text(text = "Select None")
                    }
                }
                LazyColumn {
                    items(ALL_EVENT_TYPES.sortedBy { it.displayName }, key = { it.id }) { event ->
                        val isChecked = tempSelection.contains(event.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = event.displayName)
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    tempSelection = if (checked) {
                                        tempSelection + event.id
                                    } else {
                                        tempSelection - event.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}