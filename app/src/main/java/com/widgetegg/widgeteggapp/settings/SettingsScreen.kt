package com.widgetegg.widgeteggapp.settings

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.widgetegg.widgeteggapp.MainActivity
import com.widgetegg.widgeteggapp.Routes
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tools.utilities.hasCalendarPermissions
import user.preferences.PreferencesDatastore

@Composable
fun SettingsScreen(navController: NavController, activity: MainActivity) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    // Forcibly load settings before everything else, to prevent weird UI issues with the switches
    // Probably a better way to do this
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
        settingsViewModel.updateUseAbsoluteTimeContract(preferences.getUseAbsoluteTimeContract())
        settingsViewModel.updateUseOfflineTime(preferences.getUseOfflineTime())
        settingsViewModel.updateOpenWasmeggDashboard(preferences.getOpenWasmeggDashboard())
    }

    val packageName = context.packageName
    val pm: PowerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    settingsViewModel.updateIsOptimizationDisabled(pm.isIgnoringBatteryOptimizations(packageName))

    val hasScheduleEventPermissions = hasCalendarPermissions(context)

    settingsViewModel.updateHasScheduleEventPermissions(hasScheduleEventPermissions)
    if (!hasScheduleEventPermissions) {
        settingsViewModel.updateScheduleEvents(false)
    }

    if (hasScheduleEventPermissions && settingsViewModel.scheduleEvents) {
        settingsViewModel.getCalendars(context)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.updateIsOptimizationDisabled(pm.isIgnoringBatteryOptimizations(packageName))
        val onResumeScheduleEventPermissions = hasCalendarPermissions(context)

        settingsViewModel.updateHasScheduleEventPermissions(
            onResumeScheduleEventPermissions
        )
        if (!onResumeScheduleEventPermissions) {
            settingsViewModel.updateScheduleEvents(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 25.dp, top = 50.dp)
            .semantics { contentDescription = "Settings Screen" },
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
            .padding(vertical = 100.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Settings", fontSize = TextUnit(24f, TextUnitType.Sp))

        if (!settingsViewModel.isOptimizationDisabled) {
            BatteryPermissionsContent(settingsViewModel, packageName, context)
        }

        WidgetsGeneralGroup(settingsViewModel, context)
        MissionsGeneralGroup(settingsViewModel, packageName, context, activity)
        ContractsGeneralGroup(settingsViewModel)
        NormalMissionWidgetGroup(settingsViewModel)
        LargeMissionWidgetGroup(settingsViewModel)
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
            .padding(start = 15.dp, top = 15.dp)
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

@Composable
fun WidgetsGeneralGroup(settingsViewModel: SettingsViewModel, context: Context) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Widgets General", fontSize = TextUnit(18f, TextUnitType.Sp))
        BackgroundColorPicker(settingsViewModel)
    }
}

@Composable
fun BackgroundColorPicker(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier
            .settingsRowModifier()
            .clickable {
                settingsViewModel.updateShowBackgroundColorPickerDialog(true)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Widget background color")
        BackgroundColorPickerDialog(settingsViewModel)
        AlphaTile(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color.White),
            selectedColor = settingsViewModel.widgetBackgroundColor
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BackgroundColorPickerDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showBackgroundColorPickerDialog) {
        val controller = rememberColorPickerController()
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateWidgetBackgroundColor(controller.selectedColor.value)
                settingsViewModel.updateShowBackgroundColorPickerDialog(false)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    controller = controller,
                    initialColor = settingsViewModel.widgetBackgroundColor,
                )
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller,
                    initialColor = settingsViewModel.widgetBackgroundColor
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(35.dp),
                    controller = controller,
                    initialColor = settingsViewModel.widgetBackgroundColor
                )
                AlphaTile(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    controller = controller
                )
            }
        }
    }
}

@Composable
fun MissionsGeneralGroup(
    settingsViewModel: SettingsViewModel,
    packageName: String,
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
        ScheduleEventsRow(settingsViewModel, packageName, context, activity)
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
            Text(text = "Show absolute time")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Absolute time info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowAbsoluteTimeMissionDialog(true)
                    }
            )
            AbsoluteTimeMissionDialog(settingsViewModel)
        }

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

@Composable
fun AbsoluteTimeMissionDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showAbsoluteTimeMissionDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowAbsoluteTimeMissionDialog(false)
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
                        If a ship has less than 24 hours left, show the time of return instead of the time remaining.
                        
                        Only applies to widgets that show a time.
                    """.trimIndent()
                )
            }
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
            Text(text = "Including 24hrs")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Absolute time plus day info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowAbsoluteTimePlusDayDialog(true)
                    }
            )
            AbsoluteTimePlusDayDialog(settingsViewModel)
        }

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

@Composable
fun AbsoluteTimePlusDayDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showAbsoluteTimePlusDayDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowAbsoluteTimePlusDayDialog(false)
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
                        Show the time of return even if there are more than 24 hours left.
                        
                        Show absolute time must be enabled for this to take effect. Only applies to widgets that show a time.
                    """.trimIndent()
                )
            }
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
                        Tapping any mission widget will open Egg, Inc. instead of manually refreshing all widgets.
                        
                        Automatic widget updates every 15 minutes will still happen independent of this setting.
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
fun ScheduleEventsRow(
    settingsViewModel: SettingsViewModel,
    packageName: String,
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
            Text(text = "Schedule calendar events")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Schedule events info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowScheduleEventsDialog(true)
                    }
            )
            ScheduleEventsDialog(settingsViewModel, packageName, context)
        }

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

@Composable
fun ScheduleEventsDialog(
    settingsViewModel: SettingsViewModel,
    packageName: String,
    context: Context
) {
    if (settingsViewModel.showScheduleEventsDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowScheduleEventsDialog(false)
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
                        The app will schedule a calendar event and reminder at the time of return for any in-flight ship.
                        
                        If you have previously denied calendar permissions, you will need to manually enable it within app settings.
                    """.trimIndent()
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 5.dp),
                    onClick = {
                        settingsViewModel.updateShowScheduleEventsDialog(false)
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
        Text(text = "Show target artifact")

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
        Text(text = "Show fueling ship")

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
        Text(text = "Show target artifact")

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
            Text(text = "Show tank levels")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Tank levels info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowTankLevelsDialog(true)
                    }
            )
            ShowTankLevelsDialog(settingsViewModel)
        }

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

@Composable
fun ShowTankLevelsDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showTankLevelsDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowTankLevelsDialog(false)
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
                        Show the fuel tank levels in the last slot of the widget. Will take the place of the fueling ship if it exists.
                    """.trimIndent()
                )
            }
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
            Text(text = "Use slider capacity")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Slider percent info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowSliderCapacityDialog(true)
                    }
            )
            ShowSliderCapacityDialog(settingsViewModel)
        }

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

@Composable
fun ShowSliderCapacityDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showSliderCapacityDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowSliderCapacityDialog(false)
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
                        The fuel bar percentage filled is based on the tank slider for that individual fuel, instead of the overall tank capacity.
                        
                        Show tank levels must be enabled for this to take effect.
                    """.trimIndent()
                )
            }
        }
    }
}

// CONTRACTS

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
            Text(text = "Show absolute time")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Absolute time info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowAbsoluteTimeContractDialog(true)
                    }
            )
            AbsoluteTimeContractDialog(settingsViewModel)
        }

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

@Composable
fun AbsoluteTimeContractDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showAbsoluteTimeContractDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowAbsoluteTimeContractDialog(false)
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
                        Show the estimated completion time of a contract instead of the estimated time remaining.
                    """.trimIndent()
                )
            }
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
            Text(text = "Show offline completion time")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Offline time info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowOfflineTimeDialog(true)
                    }
            )
            OfflineTimeDialog(settingsViewModel)
        }

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

@Composable
fun OfflineTimeDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showOfflineTimeDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowOfflineTimeDialog(false)
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
                        Show an estimated completion time that includes the offline contribution of all co-op members.
                    """.trimIndent()
                )
            }
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
            Text(text = "Open eicoop dashboard")
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Open wasmegg dashboard info",
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(15.dp)
                    .clickable {
                        settingsViewModel.updateShowOpenWasmeggDashboardDialog(true)
                    }
            )
            OpenWasmeggDashboardDialog(settingsViewModel)
        }

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

@Composable
fun OpenWasmeggDashboardDialog(settingsViewModel: SettingsViewModel) {
    if (settingsViewModel.showOpenWasmeggDashboardDialog) {
        Dialog(
            onDismissRequest = {
                settingsViewModel.updateShowOpenWasmeggDashboardDialog(false)
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
                        Tapping any contract widget will your open your eicoop dashboard in a browser, instead of manually refreshing all widgets. Must have a default browser set.
                        
                        Please note that not all browsers handle links from a widget correctly. The widget will do its best to try and open the dashboard.
                        
                        Automatic widget updates every 15 minutes will still happen independent of this setting.
                    """.trimIndent()
                )
            }
        }
    }
}

private fun Modifier.settingsRowModifier() =
    this
        .padding(top = 15.dp, start = 15.dp, end = 25.dp)
        .fillMaxWidth()

private fun Modifier.widgetGroupingModifier() =
    this
        .padding(top = 50.dp, start = 20.dp)
        .fillMaxWidth()