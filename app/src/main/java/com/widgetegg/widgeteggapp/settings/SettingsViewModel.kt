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
import widget.stats.StatsWidgetDataStore

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    // General

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

    var widgetBackgroundColor by mutableStateOf(DEFAULT_WIDGET_BACKGROUND_COLOR)
        private set

    fun updateWidgetBackgroundColor(input: Color) {
        widgetBackgroundColor = input
        // Color updates need runBlocking to ensure proper widget state updates
        runBlocking {
            preferences.saveWidgetBackgroundColor(input)
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().updateMissionWidgetDataStore(context, backgroundColor = input)
            ContractWidgetDataStore().updateContractWidgetDataStore(
                context,
                backgroundColor = input
            )
            StatsWidgetDataStore().updateStatsWidgetDataStore(context, backgroundColor = input)
        }
    }

    var showBackgroundColorPickerDialog by mutableStateOf(false)
        private set

    fun updateShowBackgroundColorPickerDialog(input: Boolean) {
        showBackgroundColorPickerDialog = input
    }

    var widgetTextColor by mutableStateOf(DEFAULT_WIDGET_TEXT_COLOR)
        private set

    fun updateWidgetTextColor(input: Color) {
        widgetTextColor = input
        // Color updates need runBlocking to ensure proper widget state updates
        runBlocking {
            preferences.saveWidgetTextColor(input)
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().updateMissionWidgetDataStore(context, textColor = input)
            ContractWidgetDataStore().updateContractWidgetDataStore(context, textColor = input)
            StatsWidgetDataStore().updateStatsWidgetDataStore(context, textColor = input)
        }
    }

    var showTextColorPickerDialog by mutableStateOf(false)
        private set

    fun updateShowTextColorPickerDialog(input: Boolean) {
        showTextColorPickerDialog = input
    }

    // Missions

    var useAbsoluteTimeMission by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimeMission(input: Boolean) {
        useAbsoluteTimeMission = input
        preferences.saveUseAbsoluteTimeMission(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(context, useAbsoluteTime = input)
    }

    var useAbsoluteTimePlusDay by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimePlusDay(input: Boolean) {
        useAbsoluteTimePlusDay = input
        preferences.saveUseAbsoluteTimePlusDay(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(
            context,
            useAbsoluteTimePlusDay = input
        )
    }

    var openEggInc by mutableStateOf(false)
        private set

    suspend fun updateOpenEggInc(input: Boolean) {
        openEggInc = input
        preferences.saveOpenEggInc(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(context, openEggInc = input)
    }

    var showTargetArtifactNormalWidget by mutableStateOf(false)
        private set

    suspend fun updateShowTargetArtifactNormalWidget(input: Boolean) {
        showTargetArtifactNormalWidget = input
        preferences.saveTargetArtifactNormalWidget(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(
            context,
            targetArtifactNormalWidget = input
        )
    }

    var showFuelingShip by mutableStateOf(false)
        private set

    suspend fun updateShowFuelingShip(input: Boolean) {
        showFuelingShip = input
        preferences.saveShowFuelingShip(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(context, showFuelingShip = input)
    }

    var showTargetArtifactLargeWidget by mutableStateOf(false)
        private set

    suspend fun updateShowTargetArtifactLargeWidget(input: Boolean) {
        showTargetArtifactLargeWidget = input
        preferences.saveTargetArtifactLargeWidget(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(
            context,
            targetArtifactLargeWidget = input
        )
    }

    var showTankLevels by mutableStateOf(false)
        private set

    suspend fun updateShowTankLevels(input: Boolean) {
        showTankLevels = input
        preferences.saveShowTankLevels(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(context, showTankLevels = input)
    }

    var useSliderCapacity by mutableStateOf(false)
        private set

    suspend fun updateUseSliderCapacity(input: Boolean) {
        useSliderCapacity = input
        preferences.saveUseSliderCapacity(input)
        val context = getApplication<Application>().applicationContext
        MissionWidgetDataStore().updateMissionWidgetDataStore(context, useSliderCapacity = input)
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
        // Changes to this value are tied into lifecycle events and need to wait for this operation to finish
        runBlocking {
            preferences.saveScheduleEvents(input)
            if (!scheduleEvents) {
                preferences.saveSelectedCalendar(CalendarEntry())
            }
        }
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

    // Contracts

    var useAbsoluteTimeContract by mutableStateOf(false)
        private set

    suspend fun updateUseAbsoluteTimeContract(input: Boolean) {
        useAbsoluteTimeContract = input
        preferences.saveUseAbsoluteTimeContract(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().updateContractWidgetDataStore(context, useAbsoluteTime = input)
    }

    var useOfflineTime by mutableStateOf(false)
        private set

    suspend fun updateUseOfflineTime(input: Boolean) {
        useOfflineTime = input
        preferences.saveUseOfflineTime(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().updateContractWidgetDataStore(context, useOfflineTime = input)
    }

    var openWasmeggDashboard by mutableStateOf(false)
        private set

    suspend fun updateOpenWasmeggDashboard(input: Boolean) {
        openWasmeggDashboard = input
        preferences.saveOpenWasmeggDashboard(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().updateContractWidgetDataStore(
            context,
            openWasmeggDashboard = input
        )
    }

    var showAvailableContracts by mutableStateOf(false)
        private set

    suspend fun updateShowAvailableContracts(input: Boolean) {
        showAvailableContracts = input
        preferences.saveShowAvailableContracts(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().updateContractWidgetDataStore(
            context,
            showAvailableContracts = input
        )
    }

    var showSeasonInfo by mutableStateOf(false)
        private set

    suspend fun updateShowSeasonInfo(input: Boolean) {
        showSeasonInfo = input
        preferences.saveShowSeasonInfo(input)
        val context = getApplication<Application>().applicationContext
        ContractWidgetDataStore().updateContractWidgetDataStore(context, showSeasonInfo = input)
    }

    // Stats

    var showCommunityBadges by mutableStateOf(false)
        private set

    suspend fun updateShowCommunityBadges(input: Boolean) {
        showCommunityBadges = input
        preferences.saveShowCommunityBadges(input)
        val context = getApplication<Application>().applicationContext
        StatsWidgetDataStore().updateStatsWidgetDataStore(context, showCommunityBadges = input)
    }
}