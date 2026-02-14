package widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        if (runAttemptCount > 3) {
            return Result.success()
        }

        return try {
            WidgetUpdater().updateWidgets(context)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}