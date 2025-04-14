package widget.missions.minimal

import androidx.glance.appwidget.GlanceAppWidget
import widget.missions.MissionWidgetReceiver

class MissionWidgetMinimalReceiver : MissionWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetMinimal()
}