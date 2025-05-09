package widget.missions

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

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