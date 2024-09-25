package com.example.widgetegg

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import api.fetchData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.formatMissionData
import user.preferences.PreferencesDatastore
import widget.MissionWidgetDataStore

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences: PreferencesDatastore

    init {
        val context = getApplication<Application>().applicationContext
        preferences = PreferencesDatastore(context)
    }

    var eid by mutableStateOf("")
        private set

    fun updateEid(input: String) {
        eid = input
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
            val basicRequestInfo = api.getBasicRequestInfo(eid)
            updateHasSubmitted(true)
            updateHasError(false)
            try {
                val backupResult = api.fetchBackup(basicRequestInfo)
                updateEiUserName(backupResult.userName)
                preferences.saveEiUserName(backupResult.userName)
                preferences.saveEid(eid)
                MissionWidgetDataStore().setEid(context, eid)
                updateHasSubmitted(false)
                updateEid("")
            } catch (e: Exception) {
                updateErrorMessage("Please enter a valid EID!")
                updateHasError(true)
                updateHasSubmitted(false)
            }

            try {
                //Attempt to get mission data ahead of time for any widgets
                val prefEid = preferences.getEid()
                if (prefEid.isNotBlank()) {
                    val missionResult = fetchData(prefEid)
                    val formattedMissionData = formatMissionData(missionResult)
                    preferences.saveMissionInfo(formattedMissionData)
                    MissionWidgetDataStore().setMissionInfo(context, formattedMissionData)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            MissionWidgetDataStore().clearAllData(context)
            preferences.clearPreferences()
            updateHasSubmitted(false)
            updateHasError(false)
            updateEid("")
            updateEiUserName("")
        }
    }
}