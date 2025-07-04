package widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import api.fetchBackupData
import api.fetchContractData
import api.fetchMissionData
import data.MissionInfoEntry
import ei.Ei
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import tools.utilities.formatContractData
import tools.utilities.formatMissionData
import tools.utilities.formatStatsData
import tools.utilities.formatTankInfo
import tools.utilities.scheduleCalendarEvents
import tools.utilities.updateFuelingMission
import user.preferences.PreferencesDatastore
import widget.contracts.ContractWidgetDataStore
import widget.contracts.active.ContractWidgetActive
import widget.missions.MissionWidgetDataStore
import widget.missions.large.MissionWidgetLarge
import widget.missions.minimal.MissionWidgetMinimal
import widget.missions.normal.MissionWidgetNormal
import widget.stats.StatsWidgetDataStore
import widget.stats.normal.StatsWidgetNormal
import java.time.Instant

class WidgetUpdater {
    suspend fun updateWidgets(context: Context) {
        val preferences = PreferencesDatastore(context)
        val prefEid = preferences.getEid()
        val hasMissionWidgets = hasMissionWidgets(context)
        val hasContractWidgets = hasContractWidgets(context)
        val hasStatsWidgets = hasStatsWidgets(context)
        val anyWidgets = listOf(hasMissionWidgets, hasContractWidgets, hasStatsWidgets)

        if (prefEid.isNotBlank() && anyWidgets.any { it }) {
            try {
                val backup = fetchBackupData(prefEid)

                coroutineScope {
                    val jobs = mutableListOf<Job>()
                    val exceptions = mutableListOf<Exception>()

                    if (hasMissionWidgets) {
                        val job = launch {
                            try {
                                updateMissions(context, preferences, backup)
                            } catch (e: Exception) {
                                exceptions.add(e)
                            }
                        }
                        jobs.add(job)
                    }

                    if (hasContractWidgets) {
                        val job = launch {
                            try {
                                updateContracts(context, preferences, backup)
                            } catch (e: Exception) {
                                exceptions.add(e)
                            }
                        }
                        jobs.add(job)
                    }

                    if (hasStatsWidgets) {
                        val job = launch {
                            try {
                                updateStats(context, preferences, backup)
                            } catch (e: Exception) {
                                exceptions.add(e)
                            }
                        }
                        jobs.add(job)
                    }

                    jobs.joinAll()

                    if (exceptions.isNotEmpty()) {
                        val messages = exceptions.joinToString(", ") { e -> e.message.toString() }
                        throw Exception("Errors while updating data: $messages")
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun updateMissions(
        context: Context,
        preferences: PreferencesDatastore,
        backup: Ei.Backup
    ) {
        var prefMissionInfo = preferences.getMissionInfo()
        var prefTankInfo = preferences.getTankInfo()

        val prefEid = preferences.getEid()
        val prefEiUserName = preferences.getEiUserName()
        val prefUseAbsoluteTime = preferences.getUseAbsoluteTimeMission()
        val prefUseAbsoluteTimePlusDay = preferences.getUseAbsoluteTimePlusDay()
        val prefTargetArtifactNormalWidget = preferences.getTargetArtifactNormalWidget()
        val prefTargetArtifactLargeWidget = preferences.getTargetArtifactLargeWidget()
        val prefShowFuelingShip = preferences.getShowFuelingShip()
        val prefOpenEggInc = preferences.getOpenEggInc()
        val prefShowTankLevels = preferences.getShowTankLevels()
        val prefUseSliderCapacity = preferences.getUseSliderCapacity()
        val prefScheduleEvents = preferences.getScheduleEvents()
        val prefSelectedCalendar = preferences.getSelectedCalendar()
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()

        try {
            if (prefEid.isNotBlank()) {
                if (shouldMakeApiCall(prefMissionInfo)) {
                    val missionInfo = fetchMissionData(prefEid)
                    // Get a new list of active missions and append fueling mission
                    prefMissionInfo = formatMissionData(missionInfo, backup)
                } else {
                    // Replace existing formatted missions with most recent fueling mission from backup
                    prefMissionInfo = updateFuelingMission(prefMissionInfo, backup)
                }

                prefTankInfo = formatTankInfo(backup)

                if (prefScheduleEvents) {
                    scheduleCalendarEvents(
                        context,
                        prefMissionInfo,
                        prefEiUserName,
                        prefSelectedCalendar
                    )
                }

                // Mission data and tank fuels need to get saved back to preferences because they are changing regularly
                // When a widget is initialized it needs to pull data from preferences before it can save into local widget state
                // If these items aren't updated in preferences, then a new widget will display old/outdated info
                // Figuring out how to get all widgets and preferences to read from the same data store might fix this
                preferences.saveMissionInfo(prefMissionInfo)
                preferences.saveTankInfo(prefTankInfo)
                MissionWidgetDataStore().setMissionInfo(context, prefMissionInfo)
                MissionWidgetDataStore().setTankInfo(context, prefTankInfo)

                MissionWidgetDataStore().setEid(context, prefEid)
                MissionWidgetDataStore().setUseAbsoluteTime(context, prefUseAbsoluteTime)
                MissionWidgetDataStore().setUseAbsoluteTimePlusDay(
                    context,
                    prefUseAbsoluteTimePlusDay
                )
                MissionWidgetDataStore().setTargetArtifactNormalWidget(
                    context,
                    prefTargetArtifactNormalWidget
                )
                MissionWidgetDataStore().setTargetArtifactLargeWidget(
                    context,
                    prefTargetArtifactLargeWidget
                )
                MissionWidgetDataStore().setShowFuelingShip(context, prefShowFuelingShip)
                MissionWidgetDataStore().setOpenEggInc(context, prefOpenEggInc)
                MissionWidgetDataStore().setShowTankLevels(context, prefShowTankLevels)
                MissionWidgetDataStore().setUseSliderCapacity(context, prefUseSliderCapacity)
                MissionWidgetDataStore().setBackgroundColor(context, prefWidgetBackgroundColor)
                MissionWidgetDataStore().setTextColor(context, prefWidgetTextColor)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun updateContracts(
        context: Context,
        preferences: PreferencesDatastore,
        backup: Ei.Backup
    ) {
        var prefContractInfo = preferences.getContractInfo()

        val prefEid = preferences.getEid()
        val prefUseAbsoluteTime = preferences.getUseAbsoluteTimeContract()
        val prefUseOfflineTime = preferences.getUseOfflineTime()
        val prefOpenWasmeggDashboard = preferences.getOpenWasmeggDashboard()
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()

        try {
            if (prefEid.isNotBlank()) {
                val contractInfo = fetchContractData(backup)
                prefContractInfo = formatContractData(contractInfo)

                preferences.saveContractInfo(prefContractInfo)

                ContractWidgetDataStore().setEid(context, prefEid)
                ContractWidgetDataStore().setContractInfo(context, prefContractInfo)
                ContractWidgetDataStore().setUseAbsoluteTime(context, prefUseAbsoluteTime)
                ContractWidgetDataStore().setUseOfflineTime(context, prefUseOfflineTime)
                ContractWidgetDataStore().setOpenWasmeggDashboard(
                    context,
                    prefOpenWasmeggDashboard
                )
                ContractWidgetDataStore().setBackgroundColor(context, prefWidgetBackgroundColor)
                ContractWidgetDataStore().setTextColor(context, prefWidgetTextColor)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun updateStats(
        context: Context,
        preferences: PreferencesDatastore,
        backup: Ei.Backup
    ) {
        var prefStatsInfo = preferences.getStatsInfo()

        val prefEid = preferences.getEid()
        val prefEiUserName = preferences.getEiUserName()
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()

        try {
            if (prefEid.isNotBlank()) {
                prefStatsInfo = formatStatsData(backup)

                preferences.saveStatsInfo(prefStatsInfo)

                StatsWidgetDataStore().setEid(context, prefEid)
                StatsWidgetDataStore().setEiUserName(context, prefEiUserName)
                StatsWidgetDataStore().setStatsInfo(context, prefStatsInfo)
                StatsWidgetDataStore().setBackgroundColor(context, prefWidgetBackgroundColor)
                StatsWidgetDataStore().setTextColor(context, prefWidgetTextColor)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun numOfActiveMissions(missions: List<MissionInfoEntry>): Int {
        return missions.count { mission ->
            mission.identifier.isNotBlank()
        }
    }

    private fun anyMissionsComplete(missions: List<MissionInfoEntry>): Boolean {
        return missions.any { mission ->
            mission.identifier.isNotBlank() && mission.secondsRemaining - (Instant.now().epochSecond - mission.date) <= 0
        }
    }

    private suspend fun hasMissionWidgets(context: Context): Boolean {
        return GlanceAppWidgetManager(context).getGlanceIds(
            MissionWidgetLarge::class.java
        ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
            MissionWidgetNormal::class.java
        ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
            MissionWidgetMinimal::class.java
        ).isNotEmpty()
    }

    private suspend fun hasContractWidgets(context: Context): Boolean {
        return GlanceAppWidgetManager(context).getGlanceIds(
            ContractWidgetActive::class.java
        ).isNotEmpty()
    }

    private suspend fun hasStatsWidgets(context: Context): Boolean {
        return GlanceAppWidgetManager(context).getGlanceIds(
            StatsWidgetNormal::class.java
        ).isNotEmpty()
    }

    private fun shouldMakeApiCall(preferencesMissionData: List<MissionInfoEntry>): Boolean {
        // To help reduce load on auxbrain, only make an api call if:
        // preferencesMissionData has less than 3 active missions, meaning all active missions haven't been saved
        // preferencesMissionData has complete missions, meaning we need to fetch new active missions
        return (numOfActiveMissions(preferencesMissionData) < 3 || anyMissionsComplete(
            preferencesMissionData
        ))
    }
}