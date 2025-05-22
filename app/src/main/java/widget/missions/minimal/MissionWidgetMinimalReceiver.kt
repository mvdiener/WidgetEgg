package widget.missions.minimal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class MissionWidgetMinimalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetMinimal()
}