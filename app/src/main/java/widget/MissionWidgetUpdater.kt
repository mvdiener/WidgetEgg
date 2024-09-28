package widget

import android.content.Context
import api.fetchData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.formatMissionData
import user.preferences.PreferencesDatastore
import java.time.Instant

class MissionWidgetUpdater {
    fun updateMissions(context: Context) {
        runBlocking {
            val preferences = PreferencesDatastore(context)
            var preferencesMissionData = preferences.getMissionInfo()
            val prefEid = preferences.getEid()
            val prefUseAbsoluteTime = preferences.getUseAbsoluteTime()
            val prefTargetArtifactSmall = preferences.getTargetArtifactSmall()

            try {
                if (prefEid.isNotBlank()) {
                    // Only make an api call if:
                    // preferencesMissionData is has less than 3 active missions, meaning all active missions haven't been saved OR
                    // preferencesMissionData has complete missions, meaning we need to fetch new active missions
                    if (preferencesMissionData.size < 3 || anyMissionsComplete(
                            preferencesMissionData
                        )
                    ) {
                        val missionInfo = fetchData(prefEid)
                        preferencesMissionData = formatMissionData(missionInfo)
                    }
                    preferences.saveMissionInfo(preferencesMissionData)
                    MissionWidgetDataStore().setMissionInfo(context, preferencesMissionData)

                    MissionWidgetDataStore().setEid(context, prefEid)
                    MissionWidgetDataStore().setUseAbsoluteTime(context, prefUseAbsoluteTime)
                    MissionWidgetDataStore().setTargetArtifactSmall(
                        context,
                        prefTargetArtifactSmall
                    )
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun anyMissionsComplete(missions: List<MissionInfoEntry>): Boolean {
        return missions.any { mission ->
            mission.secondsRemaining - (Instant.now().epochSecond - mission.date) <= 0
        }
    }
}