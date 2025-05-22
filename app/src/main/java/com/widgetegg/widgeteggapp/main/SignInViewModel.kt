package com.widgetegg.widgeteggapp.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import api.fetchBackupData
import api.fetchContractData
import api.fetchMissionData
import ei.Ei
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.formatContractData
import tools.utilities.formatMissionData
import tools.utilities.formatTankInfo
import user.preferences.PreferencesDatastore
import widget.contracts.ContractWidgetDataStore
import widget.missions.MissionWidgetDataStore

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    var eid by mutableStateOf("")
        private set

    fun updateEid(input: String) {
        eid = input.trim()
    }

    var eiUserName by mutableStateOf("")
        private set

    fun updateEiUserName(input: String) {
        eiUserName = input
    }

    var errorMessage by mutableStateOf("")
        private set

    private fun updateErrorMessage(input: String) {
        errorMessage = input
    }

    var hasError by mutableStateOf(false)
        private set

    private fun updateHasError(input: Boolean) {
        hasError = input
    }

    var hasSubmitted by mutableStateOf(false)
        private set

    private fun updateHasSubmitted(input: Boolean) {
        hasSubmitted = input
    }

    var showSignoutConfirmDialog by mutableStateOf(false)
        private set

    fun updateShowSignoutConfirmDialog(input: Boolean) {
        showSignoutConfirmDialog = input
    }

    var showFindMyEidDialog by mutableStateOf(false)
        private set

    fun updateShowFindMyEidDialog(input: Boolean) {
        showFindMyEidDialog = input
    }

    var showWhatNextDialog by mutableStateOf(false)
        private set

    fun updateShowWhatNextDialog(input: Boolean) {
        showWhatNextDialog = input
    }

    fun getBackupData() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            updateHasSubmitted(true)
            updateHasError(false)
            try {
                val backupResult = fetchBackupData(eid)
                updateEiUserName(backupResult.userName)
                preferences.saveEiUserName(backupResult.userName)
                preferences.saveEid(eid)
                MissionWidgetDataStore().setEid(context, eid)
                ContractWidgetDataStore().setEid(context, eid)
                updateHasSubmitted(false)
                updateEid("")
            } catch (_: Exception) {
                updateErrorMessage("Please enter a valid EID!")
                updateHasError(true)
                updateHasSubmitted(false)
            }

            try {
                //Attempt to get data ahead of time for any widgets
                val prefEid = preferences.getEid()
                if (prefEid.isNotBlank()) {
                    val backupResult = fetchBackupData(prefEid)
                    val missionResult = fetchMissionData(prefEid)
                    val contractResult = fetchContractData(prefEid, backupResult)
                    val formattedMissionData = formatMissionData(missionResult, backupResult)
                    val formattedTankInfo = formatTankInfo(backupResult)
                    val formattedContractInfo = formatContractData(contractResult)
                    preferences.saveMissionInfo(formattedMissionData)
                    preferences.saveTankInfo(formattedTankInfo)
                    preferences.saveContractInfo(formattedContractInfo)
                    MissionWidgetDataStore().setMissionInfo(context, formattedMissionData)
                    MissionWidgetDataStore().setTankInfo(context, formattedTankInfo)
                    ContractWidgetDataStore().setContractInfo(context, formattedContractInfo)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().clearAllData(context)
            ContractWidgetDataStore().clearAllData(context)
            preferences.clearPreferences()
            updateHasSubmitted(false)
            updateHasError(false)
            updateEid("")
            updateEiUserName("")
        }
    }
}