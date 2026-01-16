package widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import api.fetchBackupData
import api.fetchContractsArchive
import api.fetchContractData
import api.fetchMissionData
import api.fetchPeriodicalsData
import data.MissionInfoEntry
import data.PeriodicalsData
import ei.Ei
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import tools.utilities.formatContractData
import tools.utilities.formatCustomEggs
import tools.utilities.formatMissionData
import tools.utilities.formatPeriodicalsContracts
import tools.utilities.formatSeasonInfo
import tools.utilities.formatStatsData
import tools.utilities.formatTankInfo
import tools.utilities.removeCalendarEvents
import tools.utilities.scheduleCalendarEvents
import tools.utilities.updateFuelingMission
import user.preferences.PreferencesDatastore
import widget.contracts.ContractWidgetDataStore
import widget.contracts.active.ContractWidgetActive
import widget.contracts.large.ContractWidgetLarge
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
        val allWidgets =
            listOf(hasMissionWidgets, hasVirtueMissionWidgets, hasContractWidgets, hasStatsWidgets)

        if (prefEid.isNotBlank() && allWidgets.any { it }) {
            try {
                val backup = fetchBackupData(prefEid)

                val periodicalsInfo =
                    if (hasContractWidgets || hasStatsWidgets) fetchPeriodicalsData(prefEid) else null

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
                                updateContracts(context, preferences, backup, periodicalsInfo!!)
                            } catch (e: Exception) {
                                exceptions.add(e)
                            }
                        }
                        jobs.add(job)
                    }

                    if (hasStatsWidgets) {
                        val job = launch {
                            try {
                                updateStats(context, preferences, backup, periodicalsInfo!!)
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
        context: Context, preferences: PreferencesDatastore, backup: Ei.Backup
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
                    val missionInfo = fetchMissionData(prefEid, backup.virtue.resets)
                    // Get a new list of active missions and append fueling mission
                    if (hasMissionWidgets) {
                        prefMissionInfo = formatMissionData(missionInfo, backup)
                    }
                    if (hasVirtueMissionWidgets) {
                        prefVirtueMissionInfo = formatMissionData(missionInfo, backup, true)
                    }
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
                        context, prefMissionInfo, backup.userName, prefSelectedCalendar
                    )

                    scheduleCalendarEvents(
                        context, prefVirtueMissionInfo, backup.userName, prefSelectedCalendar, true
                    )

                    removeCalendarEvents(context, prefSelectedCalendar)
                }

                // Mission data and tank fuels need to get saved back to preferences because they are changing regularly
                // When a widget is initialized it needs to pull data from preferences before it can save into local widget state
                // If these items aren't updated in preferences, then a new widget will display old/outdated info
                // Figuring out how to get all widgets and preferences to read from the same data store might fix this
                preferences.saveMissionInfo(prefMissionInfo)
                preferences.saveVirtueMissionInfo(prefVirtueMissionInfo)
                preferences.saveTankInfo(prefTankInfo)
                preferences.saveVirtueTankInfo(prefVirtueTankInfo)
                MissionWidgetDataStore().updateMissionWidgetDataStore(
                    context,
                    eid = prefEid,
                    missionInfo = prefMissionInfo,
                    virtueMissionInfo = prefVirtueMissionInfo,
                    tankInfo = prefTankInfo,
                    virtueTankInfo = prefVirtueTankInfo,
                    useAbsoluteTime = prefUseAbsoluteTime,
                    useAbsoluteTimePlusDay = prefUseAbsoluteTimePlusDay,
                    targetArtifactNormalWidget = prefTargetArtifactNormalWidget,
                    targetArtifactLargeWidget = prefTargetArtifactLargeWidget,
                    showFuelingShip = prefShowFuelingShip,
                    openEggInc = prefOpenEggInc,
                    showTankLevels = prefShowTankLevels,
                    useSliderCapacity = prefUseSliderCapacity,
                    backgroundColor = prefWidgetBackgroundColor,
                    textColor = prefWidgetTextColor
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun updateContracts(
        context: Context,
        preferences: PreferencesDatastore,
        backup: Ei.Backup,
        periodicalsInfo: PeriodicalsData
    ) {
        var prefContractInfo = preferences.getContractInfo()
        var prefPeriodicalsContractInfo = preferences.getPeriodicalsContractInfo()
        var prefSeasonInfo = preferences.getSeasonInfo()
        var prefCustomEggsInfo = preferences.getCustomEggs()

        val prefEid = preferences.getEid()
        val prefUseAbsoluteTime = preferences.getUseAbsoluteTimeContract()
        val prefUseOfflineTime = preferences.getUseOfflineTime()
        val prefShowAvailableContracts = preferences.getShowAvailableContracts()
        val prefShowSeasonInfo = preferences.getShowSeasonInfo()
        val prefOpenWasmeggDashboard = preferences.getOpenWasmeggDashboard()
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()

        try {
            if (prefEid.isNotBlank()) {
                val contractInfo = fetchContractData(backup)
                val contractsArchiveInfo = fetchContractsArchive(prefEid)

                prefPeriodicalsContractInfo =
                    formatPeriodicalsContracts(periodicalsInfo, backup, contractsArchiveInfo)
                prefContractInfo =
                    formatContractData(
                        contractInfo,
                        backup.userName,
                        prefPeriodicalsContractInfo,
                        contractsArchiveInfo
                    )
                prefSeasonInfo = formatSeasonInfo(periodicalsInfo, backup)
                prefCustomEggsInfo = formatCustomEggs(periodicalsInfo)


                preferences.saveContractInfo(prefContractInfo)
                preferences.savePeriodicalsContractInfo(prefPeriodicalsContractInfo)
                preferences.saveSeasonInfo(prefSeasonInfo)
                preferences.saveCustomEggs(prefCustomEggsInfo)

                ContractWidgetDataStore().updateContractWidgetDataStore(
                    context,
                    eid = prefEid,
                    contractInfo = prefContractInfo,
                    periodicalsContractInfo = prefPeriodicalsContractInfo,
                    seasonInfo = prefSeasonInfo,
                    useAbsoluteTime = prefUseAbsoluteTime,
                    useOfflineTime = prefUseOfflineTime,
                    showAvailableContracts = prefShowAvailableContracts,
                    showSeasonInfo = prefShowSeasonInfo,
                    openWasmeggDashboard = prefOpenWasmeggDashboard,
                    backgroundColor = prefWidgetBackgroundColor,
                    textColor = prefWidgetTextColor,
                    customEggs = prefCustomEggsInfo
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun updateStats(
        context: Context,
        preferences: PreferencesDatastore,
        backup: Ei.Backup,
        periodicalsInfo: PeriodicalsData
    ) {
        var prefStatsInfo = preferences.getStatsInfo()
        var prefCustomEggs = preferences.getCustomEggs()

        val prefEid = preferences.getEid()
        val prefWidgetBackgroundColor = preferences.getWidgetBackgroundColor()
        val prefWidgetTextColor = preferences.getWidgetTextColor()
        val prefShowCommunityBadges = preferences.getShowCommunityBadges()

        try {
            if (prefEid.isNotBlank()) {
                prefStatsInfo = formatStatsData(backup)
                prefCustomEggs = formatCustomEggs(periodicalsInfo)

                preferences.saveStatsInfo(prefStatsInfo)
                preferences.saveCustomEggs(prefCustomEggs)

                StatsWidgetDataStore().updateStatsWidgetDataStore(
                    context,
                    eid = prefEid,
                    eiUserName = backup.userName,
                    statsInfo = prefStatsInfo,
                    backgroundColor = prefWidgetBackgroundColor,
                    textColor = prefWidgetTextColor,
                    showCommunityBadges = prefShowCommunityBadges,
                    customEggs = prefCustomEggs
                )
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
        ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
            ContractWidgetLarge::class.java
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
            ))
        val virtueMissionUpdateNeeded =
            hasVirtueMissionWidgets && (numOfActiveMissions(preferencesVirtueMissionInfo) < 3 || anyMissionsComplete(
                preferencesVirtueMissionInfo
            ))
        return missionUpdateNeeded || virtueMissionUpdateNeeded
    }
}