package widget.contracts.active

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

open class ContractWidgetActiveReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContractWidgetActive()
}