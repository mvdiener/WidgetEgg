package widget.virtue

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.VirtueInfo
import kotlinx.serialization.json.Json
import widget.virtue.normal.VirtueWidgetNormal

data object VirtueWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val VIRTUE_INFO = stringPreferencesKey("widgetVirtueInfo")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
}

class VirtueWidgetDataStore {
    suspend fun updateVirtueWidgetDataStore(
        context: Context,
        eid: String? = null,
        virtueInfo: VirtueInfo? = null,
        backgroundColor: Color? = null,
        textColor: Color? = null
    ) {
        val virtueWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueWidgetNormal::class.java)

        virtueWidgetIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                eid?.let { prefs[VirtueWidgetDataStorePreferencesKeys.EID] = it }
                virtueInfo?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.VIRTUE_INFO] =
                        Json.encodeToString(it)
                }
                backgroundColor?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        it.toArgb()
                }
                textColor?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] = it.toArgb()
                }
            }
        }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val virtueWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueWidgetNormal::class.java)
        (virtueWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun updateAllWidgets(context: Context) {
        VirtueWidgetNormal().updateAll(context)
    }
}