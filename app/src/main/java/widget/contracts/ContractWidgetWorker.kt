package widget.contracts

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ContractWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        var result: Result = Result.success()

        if (runAttemptCount > 3) {
            return Result.success()
        }

        try {
            ContractWidgetUpdater().updateContracts(context)
        } catch (e: Exception) {
            result = Result.retry()
        }

        return result
    }
}