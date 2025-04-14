package widget.contracts

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import widget.contracts.normal.ContractWidgetNormal
import java.util.concurrent.TimeUnit

open class ContractWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContractWidgetNormal()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        scheduleUpdate(context)
    }

    private fun scheduleUpdate(context: Context) {
        // Update widget every 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<ContractWidgetWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 45, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "contract_widget_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}