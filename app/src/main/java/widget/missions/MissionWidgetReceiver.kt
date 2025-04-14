package widget.missions

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import widget.missions.normal.MissionWidgetNormal
import java.util.concurrent.TimeUnit

open class MissionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetNormal()

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
        val workRequest = PeriodicWorkRequestBuilder<MissionWidgetWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 45, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "mission_widget_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}