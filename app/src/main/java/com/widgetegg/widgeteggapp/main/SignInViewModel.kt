package com.widgetegg.widgeteggapp.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import api.fetchBackupData
import api.fetchContractsArchive
import api.fetchContractData
import api.fetchMissionData
import api.fetchPeriodicalsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.formatContractData
import tools.utilities.formatCustomEggs
import tools.utilities.formatMissionData
import tools.utilities.formatPeriodicalsContracts
import tools.utilities.formatSeasonInfo
import tools.utilities.formatStatsData
import tools.utilities.formatTankInfo
import user.preferences.PreferencesDatastore
import widget.contracts.ContractWidgetDataStore
import widget.missions.MissionWidgetDataStore
import widget.stats.StatsWidgetDataStore

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
                MissionWidgetDataStore().updateMissionWidgetDataStore(context, eid = eid)
                ContractWidgetDataStore().updateContractWidgetDataStore(context, eid = eid)
                StatsWidgetDataStore().updateStatsWidgetDataStore(
                    context,
                    eid = eid,
                    eiUserName = backupResult.userName
                )
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
                    val missionResult = fetchMissionData(prefEid, backupResult.virtue.resets)
                    val contractResult = fetchContractData(backupResult)
                    val periodicalsResult = fetchPeriodicalsData(prefEid)
                    val contractsArchiveResult = fetchContractsArchive(prefEid)
                    val formattedMissionData = formatMissionData(missionResult, backupResult)
                    val formattedVirtueMissionData =
                        formatMissionData(missionResult, backupResult, true)
                    val formattedTankInfo = formatTankInfo(backupResult)
                    val formattedVirtueTankInfo = formatTankInfo(backupResult, true)
                    val formattedPeriodicalsContracts =
                        formatPeriodicalsContracts(
                            periodicalsResult,
                            backupResult,
                            contractsArchiveResult,
                            null
                        )
                    val formattedContractData =
                        formatContractData(
                            contractResult,
                            backupResult.userName,
                            formattedPeriodicalsContracts
                        )
                    val formattedSeasonInfo = formatSeasonInfo(
                        periodicalsResult,
                        backupResult
                    )
                    val formattedStatsData = formatStatsData(backupResult)
                    val formattedCustomEggs = formatCustomEggs(periodicalsResult)
                    preferences.saveMissionInfo(formattedMissionData)
                    preferences.saveVirtueMissionInfo(formattedVirtueMissionData)
                    preferences.saveTankInfo(formattedTankInfo)
                    preferences.saveVirtueTankInfo(formattedVirtueTankInfo)
                    preferences.saveContractInfo(formattedContractData)
                    preferences.savePeriodicalsContractInfo(formattedPeriodicalsContracts)
                    preferences.saveSeasonInfo(formattedSeasonInfo)
                    preferences.saveStatsInfo(formattedStatsData)
                    preferences.saveCustomEggs(formattedCustomEggs)
                    MissionWidgetDataStore().updateMissionWidgetDataStore(
                        context,
                        missionInfo = formattedMissionData,
                        virtueMissionInfo = formattedVirtueMissionData,
                        tankInfo = formattedTankInfo,
                        virtueTankInfo = formattedVirtueTankInfo
                    )
                    ContractWidgetDataStore().updateContractWidgetDataStore(
                        context,
                        contractInfo = formattedContractData,
                        periodicalsContractInfo = formattedPeriodicalsContracts,
                        seasonInfo = formattedSeasonInfo,
                        customEggs = formattedCustomEggs
                    )
                    StatsWidgetDataStore().updateStatsWidgetDataStore(
                        context,
                        statsInfo = formattedStatsData,
                        customEggs = formattedCustomEggs
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            val context = getApplication<Application>().applicationContext

            val clearJob = launch(Dispatchers.IO) {
                MissionWidgetDataStore().clearAllData(context)
                ContractWidgetDataStore().clearAllData(context)
                StatsWidgetDataStore().clearAllData(context)
                preferences.clearPreferences()
            }

            clearJob.join()

            updateHasSubmitted(false)
            updateHasError(false)
            updateEid("")
            updateEiUserName("")
        }
    }
}