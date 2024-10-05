package widget

import androidx.glance.appwidget.GlanceAppWidget

class MissionWidgetMinimalReceiver : MissionWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetMinimal()
}