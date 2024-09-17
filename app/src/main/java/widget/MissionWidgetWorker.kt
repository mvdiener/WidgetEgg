package widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import api.MissionData
import api.fetchData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore
import java.time.Instant

class MissionWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        runBlocking {
            val preferences = PreferencesDatastore(context)
            var preferencesMissionData = preferences.getMissionInfo()
            val prefEid = preferences.getEid()

            if (prefEid.isNotBlank()) {
                // Only make an api call if:
                // preferencesMissionData is empty, meaning data has not been saved before OR
                // preferencesMissionData has complete missions, meaning we need to fetch new active missions
                if (preferencesMissionData.isEmpty() || anyMissionsComplete(preferencesMissionData)) {
                    val missionInfo = fetchData(prefEid)
                    preferencesMissionData = formatMissionData(missionInfo)

                }
                preferences.saveMissionInfo(preferencesMissionData)
            }

        }

        MissionWidget().updateAll(context)

        return Result.success()
    }

    private fun formatMissionData(missionInfo: MissionData): List<MissionInfoEntry> {
        var formattedMissions: List<MissionInfoEntry> = emptyList()

        missionInfo.missions.forEach { mission ->
            if (mission.identifier.isNotBlank()) {
                formattedMissions = formattedMissions.plus(
                    MissionInfoEntry(
                        secondsRemaining = if (mission.secondsRemaining >= 0) mission.secondsRemaining else 0.0,
                        missionDuration = mission.durationSeconds,
                        date = Instant.now().epochSecond
                    )
                )
            }
        }

        return formattedMissions
    }

    private fun anyMissionsComplete(missions: List<MissionInfoEntry>): Boolean {
        return missions.any { mission ->
            mission.secondsRemaining - (Instant.now().epochSecond - mission.date) <= 0
        }
    }
}