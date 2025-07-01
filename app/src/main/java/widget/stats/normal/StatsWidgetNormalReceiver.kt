package widget.stats.normal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class StatsWidgetNormalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidgetNormal()
}