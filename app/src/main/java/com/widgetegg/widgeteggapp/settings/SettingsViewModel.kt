package com.widgetegg.widgeteggapp.settings

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract.Calendars
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.CalendarEntry
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

    fun updateUseAbsoluteTimeMission(input: Boolean) {
        useAbsoluteTimeMission = input
        runBlocking {
            preferences.saveUseAbsoluteTimeMission(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setUseAbsoluteTime(context, input)
        }
    }

    var showAbsoluteTimeMissionDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimeMissionDialog(input: Boolean) {
        showAbsoluteTimeMissionDialog = input
    }

    var useAbsoluteTimePlusDay by mutableStateOf(false)
        private set

    fun updateUseAbsoluteTimePlusDay(input: Boolean) {
        useAbsoluteTimePlusDay = input
        runBlocking {
            preferences.saveUseAbsoluteTimePlusDay(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setUseAbsoluteTimePlusDay(context, input)
        }
    }

    var showAbsoluteTimePlusDayDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimePlusDayDialog(input: Boolean) {
        showAbsoluteTimePlusDayDialog = input
    }

    var openEggInc by mutableStateOf(false)
        private set

    fun updateOpenEggInc(input: Boolean) {
        openEggInc = input
        runBlocking {
            preferences.saveOpenEggInc(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setOpenEggInc(context, input)
        }
    }

    var showOpenEggIncDialog by mutableStateOf(false)
        private set

    fun updateShowOpenEggIncDialog(input: Boolean) {
        showOpenEggIncDialog = input
    }

    var showTargetArtifactNormalWidget by mutableStateOf(false)
        private set

    fun updateShowTargetArtifactNormalWidget(input: Boolean) {
        showTargetArtifactNormalWidget = input
        runBlocking {
            preferences.saveTargetArtifactNormalWidget(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setTargetArtifactNormalWidget(context, input)
        }
    }

    var showFuelingShip by mutableStateOf(false)
        private set

    fun updateShowFuelingShip(input: Boolean) {
        showFuelingShip = input
        runBlocking {
            preferences.saveShowFuelingShip(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setShowFuelingShip(context, input)
        }
    }

    var showTargetArtifactLargeWidget by mutableStateOf(false)
        private set

    fun updateShowTargetArtifactLargeWidget(input: Boolean) {
        showTargetArtifactLargeWidget = input
        runBlocking {
            preferences.saveTargetArtifactLargeWidget(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setTargetArtifactLargeWidget(context, input)
        }
    }

    var showTankLevels by mutableStateOf(false)
        private set

    fun updateShowTankLevels(input: Boolean) {
        showTankLevels = input
        runBlocking {
            preferences.saveShowTankLevels(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setShowTankLevels(context, input)
        }
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

    fun updateUseSliderCapacity(input: Boolean) {
        useSliderCapacity = input
        runBlocking {
            preferences.saveUseSliderCapacity(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setUseSliderCapacity(context, input)
        }
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

    fun updateSelectedCalendar(input: CalendarEntry) {
        selectedCalendar = input
        runBlocking {
            preferences.saveSelectedCalendar(input)
        }
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

    fun updateUseAbsoluteTimeContract(input: Boolean) {
        useAbsoluteTimeContract = input
        runBlocking {
            preferences.saveUseAbsoluteTimeContract(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            ContractWidgetDataStore().setUseAbsoluteTime(context, input)
        }
    }

    var showAbsoluteTimeContractDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimeContractDialog(input: Boolean) {
        showAbsoluteTimeContractDialog = input
    }

    var useOfflineTime by mutableStateOf(false)
        private set

    fun updateUseOfflineTime(input: Boolean) {
        useOfflineTime = input
        runBlocking {
            preferences.saveUseOfflineTime(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            ContractWidgetDataStore().setUseOfflineTime(context, input)
        }
    }

    var showOfflineTimeDialog by mutableStateOf(false)
        private set

    fun updateShowOfflineTimeDialog(input: Boolean) {
        showOfflineTimeDialog = input
    }

    var openWasmeggDashboard by mutableStateOf(false)
        private set

    fun updateOpenWasmeggDashboard(input: Boolean) {
        openWasmeggDashboard = input
        runBlocking {
            preferences.saveOpenWasmeggDashboard(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            ContractWidgetDataStore().setOpenWasmeggDashboard(context, input)
        }
    }

    var showOpenWasmeggDashboardDialog by mutableStateOf(false)
        private set

    fun updateShowOpenWasmeggDashboardDialog(input: Boolean) {
        showOpenWasmeggDashboardDialog = input
    }
}