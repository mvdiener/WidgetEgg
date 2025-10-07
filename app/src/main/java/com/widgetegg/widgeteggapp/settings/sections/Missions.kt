package com.widgetegg.widgeteggapp.settings.sections

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.MainActivity
import com.widgetegg.widgeteggapp.Routes
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsHeaderAndDescription
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import com.widgetegg.widgeteggapp.settings.settingsRowModifier
import com.widgetegg.widgeteggapp.settings.widgetGroupingModifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tools.utilities.hasCalendarPermissions
import user.preferences.PreferencesDatastore

@Composable
fun Missions(navController: NavController, activity: MainActivity) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateUseAbsoluteTimeMission(preferences.getUseAbsoluteTimeMission())
        settingsViewModel.updateOpenEggInc(preferences.getOpenEggInc())
        settingsViewModel.updateShowTargetArtifactNormalWidget(preferences.getTargetArtifactNormalWidget())
        settingsViewModel.updateShowFuelingShip(preferences.getShowFuelingShip())
        settingsViewModel.updateShowTargetArtifactLargeWidget(preferences.getTargetArtifactLargeWidget())
        settingsViewModel.updateShowTankLevels(preferences.getShowTankLevels())
        settingsViewModel.updateUseSliderCapacity(preferences.getUseSliderCapacity())
        settingsViewModel.updateUseAbsoluteTimePlusDay(preferences.getUseAbsoluteTimePlusDay())
        settingsViewModel.updateScheduleEvents(preferences.getScheduleEvents())
        settingsViewModel.updateSelectedCalendar(preferences.getSelectedCalendar())
    }

    val hasScheduleEventPermissions = hasCalendarPermissions(context)

    settingsViewModel.updateHasScheduleEventPermissions(hasScheduleEventPermissions)
    if (!hasScheduleEventPermissions) {
        settingsViewModel.updateScheduleEvents(false)
    }

    if (hasScheduleEventPermissions && settingsViewModel.scheduleEvents) {
        settingsViewModel.getCalendars(context)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val onResumeScheduleEventPermissions = hasCalendarPermissions(context)

        settingsViewModel.updateHasScheduleEventPermissions(
            onResumeScheduleEventPermissions
        )
        if (!onResumeScheduleEventPermissions) {
            settingsViewModel.updateScheduleEvents(false)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsHeader(
            text = "Back to Settings Screen",
            onClick = { navController.navigate(Routes.settingsScreen) }
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Mission Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
            MissionsGeneralGroup(settingsViewModel, context, activity)
            NormalMissionWidgetGroup(settingsViewModel)
            LargeMissionWidgetGroup(settingsViewModel)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun MissionsGeneralGroup(
    settingsViewModel: SettingsViewModel,
    context: Context,
    activity: MainActivity
) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Missions General", fontSize = TextUnit(18f, TextUnitType.Sp))
        AbsoluteTimeMissionRow(settingsViewModel)
        AbsoluteTimePlusDayRow(settingsViewModel)
        OpenEggIncRow(settingsViewModel)
        ScheduleEventsRow(settingsViewModel, context, activity)
        if (settingsViewModel.scheduleEvents) {
            CalendarsDropdownRow(settingsViewModel)
        }
    }
}

@Composable
fun NormalMissionWidgetGroup(settingsViewModel: SettingsViewModel) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Normal Mission Widget", fontSize = TextUnit(18f, TextUnitType.Sp))
        TargetArtifactNormalWidgetRow(settingsViewModel)
        ShowFuelingShipRow(settingsViewModel)
    }
}

@Composable
fun LargeMissionWidgetGroup(settingsViewModel: SettingsViewModel) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Large Mission Widget", fontSize = TextUnit(18f, TextUnitType.Sp))
        TargetArtifactLargeWidgetRow(settingsViewModel)
        ShowTankLevelsRow(settingsViewModel)
        UseSliderCapacityRow(settingsViewModel)
    }
}

@Composable
fun AbsoluteTimeMissionRow(settingsViewModel: SettingsViewModel) {
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
                "Show the time of return instead of the time remaining.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useAbsoluteTimeMission,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseAbsoluteTimeMission(!settingsViewModel.useAbsoluteTimeMission)
                        if (!settingsViewModel.useAbsoluteTimeMission) {
                            settingsViewModel.updateUseAbsoluteTimePlusDay(false)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AbsoluteTimePlusDayRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Including 24hrs",
                "Show the time of return even if there are more than 24 hours left.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useAbsoluteTimePlusDay,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseAbsoluteTimePlusDay(!settingsViewModel.useAbsoluteTimePlusDay)
                    }
                },
                enabled = settingsViewModel.useAbsoluteTimeMission
            )
        }
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
            SettingsHeaderAndDescription(
                "Open egg inc",
                "Tapping any mission widget will open Egg, Inc. instead of manually refreshing all widgets.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.openEggInc,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateOpenEggInc(!settingsViewModel.openEggInc)
                    }
                }
            )
        }
    }
}

@Composable
fun ScheduleEventsRow(
    settingsViewModel: SettingsViewModel,
    context: Context,
    activity: MainActivity
) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Schedule calendar events",
                "Schedule a calendar event for returning ships. Previously denied calendar permissions need to be manually re-enabled within app settings.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )
            Switch(
                checked = settingsViewModel.scheduleEvents,
                onCheckedChange = {
                    if (settingsViewModel.hasScheduleEventPermissions) {
                        if (!settingsViewModel.scheduleEvents) {
                            settingsViewModel.getCalendars(context)
                        }
                        settingsViewModel.updateScheduleEvents(!settingsViewModel.scheduleEvents)
                    } else {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                android.Manifest.permission.READ_CALENDAR,
                                android.Manifest.permission.WRITE_CALENDAR
                            ),
                            101
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun CalendarsDropdownRow(settingsViewModel: SettingsViewModel) {
    var isDropDownExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.clickable {
                isDropDownExpanded = true
            }
        ) {
            Text(settingsViewModel.selectedCalendar.displayName)
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Calendar dropdown",
            )

            val scope = rememberCoroutineScope()
            DropdownMenu(
                expanded = isDropDownExpanded,
                onDismissRequest = {
                    isDropDownExpanded = false
                }) {
                settingsViewModel.userCalendars.forEach { calendar ->
                    DropdownMenuItem(
                        text = {
                            Text(text = calendar.displayName)
                        },
                        onClick = {
                            scope.launch {
                                settingsViewModel.updateSelectedCalendar(calendar)
                            }
                            isDropDownExpanded = false
                        })
                }
            }
        }
    }
}

@Composable
fun TargetArtifactNormalWidgetRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsHeaderAndDescription(
            "Show target artifact",
            null,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )

        val scope = rememberCoroutineScope()
        Switch(
            checked = settingsViewModel.showTargetArtifactNormalWidget,
            onCheckedChange = {
                scope.launch {
                    settingsViewModel.updateShowTargetArtifactNormalWidget(!settingsViewModel.showTargetArtifactNormalWidget)
                }
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
        SettingsHeaderAndDescription(
            "Show fueling ship",
            null,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )

        val scope = rememberCoroutineScope()
        Switch(
            checked = settingsViewModel.showFuelingShip,
            onCheckedChange = {
                scope.launch {
                    settingsViewModel.updateShowFuelingShip(!settingsViewModel.showFuelingShip)
                }
            }
        )
    }
}

@Composable
fun TargetArtifactLargeWidgetRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsHeaderAndDescription(
            "Show target artifact",
            null,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )

        val scope = rememberCoroutineScope()
        Switch(
            checked = settingsViewModel.showTargetArtifactLargeWidget,
            onCheckedChange = {
                scope.launch {
                    settingsViewModel.updateShowTargetArtifactLargeWidget(!settingsViewModel.showTargetArtifactLargeWidget)
                }
            }
        )
    }
}

@Composable
fun ShowTankLevelsRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Show tank levels",
                "Show fuel tank levels instead of the fueling ship.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )

            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.showTankLevels,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateShowTankLevels(!settingsViewModel.showTankLevels)
                        if (!settingsViewModel.showTankLevels) {
                            settingsViewModel.updateUseSliderCapacity(false)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun UseSliderCapacityRow(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier.settingsRowModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SettingsHeaderAndDescription(
                "Use slider capacity",
                "Fuel bar percentage is based on the tank slider.",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            )

            val scope = rememberCoroutineScope()
            Switch(
                checked = settingsViewModel.useSliderCapacity,
                onCheckedChange = {
                    scope.launch {
                        settingsViewModel.updateUseSliderCapacity(!settingsViewModel.useSliderCapacity)
                    }
                },
                enabled = settingsViewModel.showTankLevels
            )
        }
    }
}