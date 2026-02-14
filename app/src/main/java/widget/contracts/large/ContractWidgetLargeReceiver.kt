package widget.contracts.large

import androidx.glance.appwidget.GlanceAppWidget
import widget.WidgetReceiver

open class ContractWidgetLargeReceiver : WidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContractWidgetLarge()
}