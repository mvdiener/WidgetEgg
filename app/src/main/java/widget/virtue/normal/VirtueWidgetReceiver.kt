package widget.virtue.normal

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

open class VirtueWidgetReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VirtueWidgetNormal()
}