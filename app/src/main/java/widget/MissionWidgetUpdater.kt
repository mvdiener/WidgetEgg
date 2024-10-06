package widget

import android.content.Context
import api.fetchData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.formatMissionData
import tools.formatTankInfo
import user.preferences.PreferencesDatastore
import java.time.Instant

class MissionWidgetUpdater {
    fun updateMissions(context: Context) {
        runBlocking {
            val preferences = PreferencesDatastore(context)
            var preferencesMissionData = preferences.getMissionInfo()
            var preferencesTankInfo = preferences.getTankInfo()

            val prefEid = preferences.getEid()
            val prefUseAbsoluteTime = preferences.getUseAbsoluteTime()
            val prefTargetArtifactNormalWidget = preferences.getTargetArtifactNormalWidget()
            val prefTargetArtifactLargeWidget = preferences.getTargetArtifactLargeWidget()
            val prefShowFuelingShip = preferences.getShowFuelingShip()
            val prefOpenEggInc = preferences.getOpenEggInc()
            val prefShowTankLevels = preferences.getShowTankLevels()

            try {
                if (prefEid.isNotBlank()) {
                    // Only make an api call if:
                    // preferencesMissionData is has less than 3 active missions, meaning all active missions haven't been saved
                    // preferencesMissionData has complete missions, meaning we need to fetch new active missions
                    // preferencesShowFuelingShip is enabled, meaning we want fresh data every time
                    if (numOfActiveMissions(preferencesMissionData) < 3 || anyMissionsComplete(
                            preferencesMissionData
                        ) || prefShowFuelingShip || prefShowTankLevels
                    ) {
                        val missionInfo = fetchData(prefEid)
                        preferencesMissionData = formatMissionData(missionInfo)
                        preferencesTankInfo = formatTankInfo(missionInfo)
                    }

                    // Mission data and tank fuels need to get saved back to preferences because they are changing regularly
                    // When a widget is initialized it needs to pull data from preferences before it can save into local widget state
                    // If these items aren't updated in preferences, then a new widget will display old/outdated info
                    // Figuring out how to get all widgets and preferences to read from the same data store might fix this
                    preferences.saveMissionInfo(preferencesMissionData)
                    preferences.saveTankInfo(preferencesTankInfo)
                    MissionWidgetDataStore().setMissionInfo(context, preferencesMissionData)
                    MissionWidgetDataStore().setTankInfo(context, preferencesTankInfo)

                    MissionWidgetDataStore().setEid(context, prefEid)
                    MissionWidgetDataStore().setUseAbsoluteTime(context, prefUseAbsoluteTime)
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
                }
            } catch (e: Exception) {
                throw e
            }
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
}