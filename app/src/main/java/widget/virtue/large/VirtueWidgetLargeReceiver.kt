package widget.virtue.large

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

open class VirtueWidgetLargeReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VirtueWidgetLarge()
}