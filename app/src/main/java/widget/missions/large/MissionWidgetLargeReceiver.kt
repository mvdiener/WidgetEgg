package widget.missions.large

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

class MissionWidgetLarge : BaseWidgetLarge(isVirtueWidget = false)
class VirtueMissionWidgetLarge : BaseWidgetLarge(isVirtueWidget = true)

class MissionWidgetLargeReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidgetLarge()
}

class VirtueMissionWidgetLargeReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VirtueMissionWidgetLarge()
}