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
import widget.missions.large.VirtueMissionWidgetLarge
import widget.missions.minimal.MissionWidgetMinimal
import widget.missions.minimal.VirtueMissionWidgetMinimal
import widget.missions.normal.MissionWidgetNormal
import widget.missions.normal.VirtueMissionWidgetNormal
import widget.stats.StatsWidgetDataStore
import widget.stats.normal.StatsWidgetNormal
import java.time.Instant

class WidgetUpdater {
    suspend fun updateWidgets(context: Context) {
        val preferences = PreferencesDatastore(context)
        val prefEid = preferences.getEid()
        val hasMissionWidgets = hasMissionWidgets(context)
        val hasVirtueMissionWidgets = hasVirtueMissionWidgets(context)
        val hasContractWidgets = hasContractWidgets(context)
        val hasStatsWidgets = hasStatsWidgets(context)
        val allWidgets = listOf(hasMissionWidgets, hasContractWidgets, hasStatsWidgets)

        if (prefEid.isNotBlank() && allWidgets.any { it }) {
            try {
                val backup = fetchBackupData(prefEid)

                coroutineScope {
                    // Update the username, in case it has changed
                    // This is only used in the main activity
                    preferences.saveEiUserName(backup.userName)

                    val jobs = mutableListOf<Job>()
                    val exceptions = mutableListOf<Exception>()

                    if (hasMissionWidgets || hasVirtueMissionWidgets) {
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
        var prefVirtueMissionInfo = preferences.getVirtueMissionInfo()
        var prefTankInfo = preferences.getTankInfo()
        var prefVirtueTankInfo = preferences.getVirtueTankInfo()

        val prefEid = preferences.getEid()
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
                val hasMissionWidgets = hasMissionWidgets(context)
                val hasVirtueMissionWidgets = hasVirtueMissionWidgets(context)
                if (shouldMakeApiCall(
                        prefMissionInfo,
                        prefVirtueMissionInfo,
                        hasMissionWidgets,
                        hasVirtueMissionWidgets
                    )
                ) {
                    val missionInfo = fetchMissionData(prefEid)
                    // Get a new list of active missions and append fueling mission
                    prefMissionInfo = formatMissionData(missionInfo, backup)
                    prefVirtueMissionInfo = formatMissionData(missionInfo, backup, true)
                } else {
                    // Replace existing formatted missions with most recent fueling mission from backup
                    prefMissionInfo = updateFuelingMission(prefMissionInfo, backup)
                    prefVirtueMissionInfo =
                        updateFuelingMission(prefVirtueMissionInfo, backup, true)
                }

                prefTankInfo = formatTankInfo(backup)
                prefVirtueTankInfo = formatTankInfo(backup, true)

                if (prefScheduleEvents) {
                    scheduleCalendarEvents(
                        context,
                        prefMissionInfo,
                        backup.userName,
                        prefSelectedCalendar
                    )

                    scheduleCalendarEvents(
                        context,
                        prefVirtueMissionInfo,
                        backup.userName,
                        prefSelectedCalendar,
                        true
                    )
                }

                // Mission data and tank fuels need to get saved back to preferences because they are changing regularly
                // When a widget is initialized it needs to pull data from preferences before it can save into local widget state
                // If these items aren't updated in preferences, then a new widget will display old/outdated info
                // Figuring out how to get all widgets and preferences to read from the same data store might fix this
                preferences.saveMissionInfo(prefMissionInfo)
                preferences.saveVirtueMissionInfo(prefVirtueMissionInfo)
                preferences.saveTankInfo(prefTankInfo)
                preferences.saveVirtueTankInfo(prefVirtueTankInfo)
                MissionWidgetDataStore().setMissionInfo(context, prefMissionInfo)
                MissionWidgetDataStore().setVirtueMissionInfo(context, prefVirtueMissionInfo)
                MissionWidgetDataStore().setTankInfo(context, prefTankInfo)
                MissionWidgetDataStore().setVirtueTankInfo(context, prefVirtueTankInfo)

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
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()
        val prefShowCommunityBadges = preferences.getShowCommunityBadges()

        try {
            if (prefEid.isNotBlank()) {
                prefStatsInfo = formatStatsData(backup)

                preferences.saveStatsInfo(prefStatsInfo)

                StatsWidgetDataStore().setEid(context, prefEid)
                StatsWidgetDataStore().setEiUserName(context, backup.userName)
                StatsWidgetDataStore().setStatsInfo(context, prefStatsInfo)
                StatsWidgetDataStore().setBackgroundColor(context, prefWidgetBackgroundColor)
                StatsWidgetDataStore().setTextColor(context, prefWidgetTextColor)
                StatsWidgetDataStore().setShowCommunityBadges(context, prefShowCommunityBadges)
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

    private suspend fun hasVirtueMissionWidgets(context: Context): Boolean {
        return GlanceAppWidgetManager(context).getGlanceIds(
            VirtueMissionWidgetLarge::class.java
        ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
            VirtueMissionWidgetNormal::class.java
        ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
            VirtueMissionWidgetMinimal::class.java
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

    private fun shouldMakeApiCall(
        preferencesMissionInfo: List<MissionInfoEntry>,
        preferencesVirtueMissionInfo: List<MissionInfoEntry>,
        hasMissionWidgets: Boolean,
        hasVirtueMissionWidgets: Boolean
    ): Boolean {
        // To help reduce load on auxbrain, only make an api call if:
        // Mission widgets exist and there are fewer than 3 active missions, or any missions are finished
        // Virtue mission widgets exist and there are fewer than 3 active virtue missions, or any virtue missions are finished
        //
        // Any of the above means we should hit the active missions endpoint to retrieve new data
        val missionUpdateNeeded =
            hasMissionWidgets && (numOfActiveMissions(preferencesMissionInfo) < 3 || anyMissionsComplete(
                preferencesMissionInfo
            )
                    )
        val virtueMissionUpdateNeeded =
            hasVirtueMissionWidgets && (numOfActiveMissions(preferencesVirtueMissionInfo) < 3 || anyMissionsComplete(
                preferencesVirtueMissionInfo
            )
                    )
        return missionUpdateNeeded || virtueMissionUpdateNeeded
    }
}