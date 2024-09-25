package widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import api.fetchData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.formatMissionData
import user.preferences.PreferencesDatastore
import java.time.Instant

class MissionWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        var result: Result = Result.success()

        if (runAttemptCount > 3) {
            return Result.success()
        }

        runBlocking {
            val preferences = PreferencesDatastore(context)
            var preferencesMissionData = preferences.getMissionInfo()
            val prefEid = preferences.getEid()

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
                }
            } catch (e: Exception) {
                result = Result.retry()
            }
        }

        return result
    }

    private fun anyMissionsComplete(missions: List<MissionInfoEntry>): Boolean {
        return missions.any { mission ->
            mission.secondsRemaining - (Instant.now().epochSecond - mission.date) <= 0
        }
    }
}