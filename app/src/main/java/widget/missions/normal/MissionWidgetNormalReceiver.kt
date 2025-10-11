package widget.missions.normal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class MissionWidgetNormal : BaseWidgetNormal(isVirtueWidget = false)
class VirtueMissionWidgetNormal : BaseWidgetNormal(isVirtueWidget = true)

class MissionWidgetNormalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetNormal()
}

class VirtueMissionWidgetNormalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VirtueMissionWidgetNormal()
}