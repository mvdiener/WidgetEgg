package widget.missions.normal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class MissionWidgetNormalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetNormal()
}