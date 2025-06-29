package com.widgetegg.widgeteggapp.settings

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract.Calendars
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.CalendarEntry
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore
import widget.contracts.ContractWidgetDataStore
import widget.missions.MissionWidgetDataStore

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    var useAbsoluteTimeMission by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimeMission(input: Boolean) {
        useAbsoluteTimeMission = input
        preferences.saveUseAbsoluteTimeMission(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setUseAbsoluteTime(context, input)
    }

    var showAbsoluteTimeMissionDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimeMissionDialog(input: Boolean) {
        showAbsoluteTimeMissionDialog = input
    }

    var useAbsoluteTimePlusDay by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimePlusDay(input: Boolean) {
        useAbsoluteTimePlusDay = input
        preferences.saveUseAbsoluteTimePlusDay(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setUseAbsoluteTimePlusDay(context, input)
    }

    var showAbsoluteTimePlusDayDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimePlusDayDialog(input: Boolean) {
        showAbsoluteTimePlusDayDialog = input
    }

    var openEggInc by mutableStateOf(false)
        private set

    suspend fun updateOpenEggInc(input: Boolean) {
        openEggInc = input
        preferences.saveOpenEggInc(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setOpenEggInc(context, input)
    }

    var showOpenEggIncDialog by mutableStateOf(false)
        private set

    fun updateShowOpenEggIncDialog(input: Boolean) {
        showOpenEggIncDialog = input
    }

    var showTargetArtifactNormalWidget by mutableStateOf(false)
        private set

    suspend fun updateShowTargetArtifactNormalWidget(input: Boolean) {
        showTargetArtifactNormalWidget = input
        preferences.saveTargetArtifactNormalWidget(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setTargetArtifactNormalWidget(context, input)
    }

    var showFuelingShip by mutableStateOf(false)
        private set

    suspend fun updateShowFuelingShip(input: Boolean) {
        showFuelingShip = input
        preferences.saveShowFuelingShip(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setShowFuelingShip(context, input)
    }

    var showTargetArtifactLargeWidget by mutableStateOf(false)
        private set

    suspend fun updateShowTargetArtifactLargeWidget(input: Boolean) {
        showTargetArtifactLargeWidget = input
        preferences.saveTargetArtifactLargeWidget(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setTargetArtifactLargeWidget(context, input)
    }

    var showTankLevels by mutableStateOf(false)
        private set

    suspend fun updateShowTankLevels(input: Boolean) {
        showTankLevels = input
        preferences.saveShowTankLevels(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setShowTankLevels(context, input)
    }

    var showTankLevelsDialog by mutableStateOf(false)
        private set

    fun updateShowTankLevelsDialog(input: Boolean) {
        showTankLevelsDialog = input
    }

    var isOptimizationDisabled by mutableStateOf(true)
        private set

    fun updateIsOptimizationDisabled(input: Boolean) {
        isOptimizationDisabled = input
    }

    var showBatteryOptimizationDialog by mutableStateOf(false)
        private set

    fun updateShowBatteryOptimizationDialog(input: Boolean) {
        showBatteryOptimizationDialog = input
    }

    var useSliderCapacity by mutableStateOf(false)
        private set

    suspend fun updateUseSliderCapacity(input: Boolean) {
        useSliderCapacity = input
        preferences.saveUseSliderCapacity(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().setUseSliderCapacity(context, input)
    }

    var showSliderCapacityDialog by mutableStateOf(false)
        private set

    fun updateShowSliderCapacityDialog(input: Boolean) {
        showSliderCapacityDialog = input
    }

    var hasScheduleEventPermissions by mutableStateOf(false)
        private set

    fun updateHasScheduleEventPermissions(input: Boolean) {
        hasScheduleEventPermissions = input
    }

    var scheduleEvents by mutableStateOf(false)
        private set

    fun updateScheduleEvents(input: Boolean) {
        scheduleEvents = input
        // This is the one setting that needs to use runBlocking
        // Changes to this value are tied into lifecycle events and need to wait for this operation to finish
        runBlocking {
            preferences.saveScheduleEvents(input)
            if (!scheduleEvents) {
                preferences.saveSelectedCalendar(CalendarEntry())
            }
        }
    }

    var showScheduleEventsDialog by mutableStateOf(false)
        private set

    fun updateShowScheduleEventsDialog(input: Boolean) {
        showScheduleEventsDialog = input
    }

    var selectedCalendar by mutableStateOf(CalendarEntry())
        private set

    suspend fun updateSelectedCalendar(input: CalendarEntry) {
        selectedCalendar = input
        preferences.saveSelectedCalendar(input)
    }

    var userCalendars by mutableStateOf(listOf(CalendarEntry()))
        private set

    private fun updateUserCalendars(input: List<CalendarEntry>) {
        userCalendars = input
    }

    fun getCalendars(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            var calendars = listOf<CalendarEntry>()

            val projection = arrayOf(
                Calendars._ID,
                Calendars.CALENDAR_DISPLAY_NAME
            )

            val selection =
                "${Calendars.CALENDAR_ACCESS_LEVEL} = ?"
            val selectionArgs = arrayOf(Calendars.CAL_ACCESS_OWNER.toString())

            val projectionIdIndex = 0
            val projectionDisplayNameIndex = 1

            val cursor: Cursor? =
                contentResolver.query(
                    Calendars.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )

            cursor?.use {
                while (it.moveToNext()) {
                    val calendarId = it.getLong(projectionIdIndex)
                    val calendarDisplayName = it.getString(projectionDisplayNameIndex)

                    calendars = calendars.plus(CalendarEntry(calendarId, calendarDisplayName))
                }
            }

            cursor?.close()
            updateUserCalendars(calendars)
        }
    }

    var useAbsoluteTimeContract by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimeContract(input: Boolean) {
        useAbsoluteTimeContract = input
        preferences.saveUseAbsoluteTimeContract(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().setUseAbsoluteTime(context, input)
    }

    var showAbsoluteTimeContractDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimeContractDialog(input: Boolean) {
        showAbsoluteTimeContractDialog = input
    }

    var useOfflineTime by mutableStateOf(false)
        private set

    suspend fun updateUseOfflineTime(input: Boolean) {
        useOfflineTime = input
        preferences.saveUseOfflineTime(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().setUseOfflineTime(context, input)
    }

    var showOfflineTimeDialog by mutableStateOf(false)
        private set

    fun updateShowOfflineTimeDialog(input: Boolean) {
        showOfflineTimeDialog = input
    }

    var openWasmeggDashboard by mutableStateOf(false)
        private set

    suspend fun updateOpenWasmeggDashboard(input: Boolean) {
        openWasmeggDashboard = input
        preferences.saveOpenWasmeggDashboard(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().setOpenWasmeggDashboard(context, input)
    }

    var showOpenWasmeggDashboardDialog by mutableStateOf(false)
        private set

    fun updateShowOpenWasmeggDashboardDialog(input: Boolean) {
        showOpenWasmeggDashboardDialog = input
    }

    var widgetBackgroundColor by mutableStateOf(DEFAULT_WIDGET_BACKGROUND_COLOR)
        private set

    suspend fun updateWidgetBackgroundColor(input: Color) {
        widgetBackgroundColor = input
        preferences.saveWidgetBackgroundColor(input)
    }

    var showBackgroundColorPickerDialog by mutableStateOf(false)
        private set

    fun updateShowBackgroundColorPickerDialog(input: Boolean) {
        showBackgroundColorPickerDialog = input
    }

    var widgetTextColor by mutableStateOf(DEFAULT_WIDGET_TEXT_COLOR)
        private set

    suspend fun updateWidgetTextColor(input: Color) {
        widgetTextColor = input
        preferences.saveWidgetTextColor(input)
    }

    var showTextColorPickerDialog by mutableStateOf(false)
        private set

    fun updateShowTextColorPickerDialog(input: Boolean) {
        showTextColorPickerDialog = input
    }
}