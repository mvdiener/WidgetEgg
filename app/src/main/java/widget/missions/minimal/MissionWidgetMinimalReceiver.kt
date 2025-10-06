package widget.missions.minimal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class MissionWidgetMinimal : BaseWidgetMinimal(isVirtueWidget = false)
class VirtueMissionWidgetMinimal : BaseWidgetMinimal(isVirtueWidget = true)

class MissionWidgetMinimalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetMinimal()
}

class VirtueMissionWidgetMinimalReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VirtueMissionWidgetMinimal()
}