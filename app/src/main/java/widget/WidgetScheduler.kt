package widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WidgetScheduler() {
    fun scheduleUpdate(context: Context) {
        cancelOldWorkers(context)
        // Update widget every 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<WidgetWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 45, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widgetegg_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    // The worker(s) have been renamed, added and removed over time
    // Just in case any old workers are running on a user's device, get rid of them
    private fun cancelOldWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val oldWorkNames = listOf("mission_widget_worker", "contract_widget_worker")

        oldWorkNames.forEach { name ->
            val worker = workManager.getWorkInfosForUniqueWork(name).get()
            if (worker.isNotEmpty()) {
                workManager.cancelUniqueWork(name)
            }
        }
    }
}