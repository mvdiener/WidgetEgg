package com.widgetegg.widgeteggapp.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore
import widget.MissionWidgetDataStore

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    var useAbsoluteTime by mutableStateOf(false)
        private set

    fun updateUseAbsoluteTime(input: Boolean) {
        useAbsoluteTime = input
        runBlocking {
            preferences.saveUseAbsoluteTime(input)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().setUseAbsoluteTime(context, input)
        }
    }

    var showAbsoluteTimeDialog by mutableStateOf(false)
        private set

    fun updateShowAbsoluteTimeDialog(input: Boolean) {
        showAbsoluteTimeDialog = input
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
}