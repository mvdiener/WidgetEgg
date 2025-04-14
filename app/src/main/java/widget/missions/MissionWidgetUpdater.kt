package widget.missions

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import api.fetchMissionData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.formatMissionData
import tools.formatTankInfo
import tools.scheduleCalendarEvents
import user.preferences.PreferencesDatastore
import widget.missions.large.MissionWidgetLarge
import widget.missions.minimal.MissionWidgetMinimal
import widget.missions.normal.MissionWidgetNormal
import java.time.Instant

class MissionWidgetUpdater {
    fun updateMissions(context: Context) {
        runBlocking {
            val preferences = PreferencesDatastore(context)

            var prefMissionInfo = preferences.getMissionInfo()
            var prefTankInfo = preferences.getTankInfo()

            val prefEid = preferences.getEid()
            val prefEiUserName = preferences.getEiUserName()
            val prefUseAbsoluteTime = preferences.getUseAbsoluteTime()
            val prefUseAbsoluteTimePlusDay = preferences.getUseAbsoluteTimePlusDay()
            val prefTargetArtifactNormalWidget = preferences.getTargetArtifactNormalWidget()
            val prefTargetArtifactLargeWidget = preferences.getTargetArtifactLargeWidget()
            val prefShowFuelingShip = preferences.getShowFuelingShip()
            val prefOpenEggInc = preferences.getOpenEggInc()
            val prefShowTankLevels = preferences.getShowTankLevels()
            val prefUseSliderCapacity = preferences.getUseSliderCapacity()
            val prefScheduleEvents = preferences.getScheduleEvents()
            val prefSelectedCalendar = preferences.getSelectedCalendar()

            try {
                if (prefEid.isNotBlank()) {
                    if (shouldMakeApiCall(context, prefMissionInfo, prefShowFuelingShip)) {
                        val missionInfo = fetchMissionData(prefEid)
                        prefMissionInfo = formatMissionData(missionInfo)
                        prefTankInfo = formatTankInfo(missionInfo)
                    }

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

    private suspend fun shouldMakeApiCall(
        context: Context,
        preferencesMissionData: List<MissionInfoEntry>,
        prefShowFuelingShip: Boolean
    ): Boolean {
        // Only make an api call if:
        // preferencesMissionData has less than 3 active missions, meaning all active missions haven't been saved
        // preferencesMissionData has complete missions, meaning we need to fetch new active missions
        // preferencesShowFuelingShip is enabled for the normal widget, meaning we want fresh data every time
        // the user has any large widgets, as this widget either needs up-to-date fueling ship or fuel tank info
        // the user has any minimal widgets, as this needs up-to-date fueling ship info
        return if (numOfActiveMissions(preferencesMissionData) < 3
            || anyMissionsComplete(
                preferencesMissionData
            )
        ) {
            true
        } else if (prefShowFuelingShip && GlanceAppWidgetManager(context).getGlanceIds(
                MissionWidgetNormal::class.java
            ).isNotEmpty()
        ) {
            true
        } else if (GlanceAppWidgetManager(context).getGlanceIds(
                MissionWidgetLarge::class.java
            ).isNotEmpty() || GlanceAppWidgetManager(context).getGlanceIds(
                MissionWidgetMinimal::class.java
            ).isNotEmpty()
        ) {
            true
        } else {
            false
        }
    }
}