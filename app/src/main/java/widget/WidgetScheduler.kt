package widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WidgetScheduler() {
    fun scheduleUpdate(context: Context) {
        cancelOldWork(context)
        // Update widget every 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<WidgetWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 45, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widgetegg_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // The worker(s) have been renamed, added and removed over time
    // Just in case any old workers are running on a user's device, get rid of them
    private fun cancelOldWork(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val oldMissionWorkerName = "mission_widget_worker"
        val oldMissionWorker = workManager.getWorkInfosForUniqueWork(oldMissionWorkerName).get()
        if (oldMissionWorker.isNotEmpty()) {
            workManager.cancelUniqueWork(oldMissionWorkerName)
        }

        val oldContractWorkerName = "contract_widget_worker"
        val oldContractWorker =
            workManager.getWorkInfosForUniqueWork(oldContractWorkerName).get()
        if (oldContractWorker.isNotEmpty()) {
            workManager.cancelUniqueWork(oldContractWorkerName)
        }
    }
}