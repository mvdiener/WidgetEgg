package widget.large

import androidx.glance.appwidget.GlanceAppWidget
import widget.MissionWidgetReceiver

class MissionWidgetLargeReceiver : MissionWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetLarge()
}