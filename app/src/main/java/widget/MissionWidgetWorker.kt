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

        try {
            MissionWidgetUpdater().updateMissions(context)
        } catch (e: Exception) {
            result = Result.retry()
        }

        return result
    }
}