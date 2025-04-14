package widget.missions.large

import androidx.glance.appwidget.GlanceAppWidget
import widget.missions.MissionWidgetReceiver

class MissionWidgetLargeReceiver : MissionWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetLarge()
}