package widget.minimal

import androidx.glance.appwidget.GlanceAppWidget
import widget.MissionWidgetReceiver

class MissionWidgetMinimalReceiver : MissionWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetMinimal()
}