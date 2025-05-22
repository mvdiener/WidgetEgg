package widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

abstract class WidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetScheduler().scheduleUpdate(context)
    }
}