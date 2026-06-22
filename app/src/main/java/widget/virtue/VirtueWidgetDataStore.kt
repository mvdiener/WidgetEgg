package widget.virtue

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.VirtueInfo
import kotlinx.serialization.json.Json
import widget.virtue.large.VirtueWidgetLarge
import widget.virtue.normal.VirtueWidgetNormal

data object VirtueWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val VIRTUE_INFO = stringPreferencesKey("widgetVirtueInfo")
    val USE_ABSOLUTE_TIME_VIRTUE_SILOS = booleanPreferencesKey("widgetUseAbsoluteTimeVirtueSilos")
    val USE_ABSOLUTE_TIME_VIRTUE_NEXT_TRUTH_EGG =
        booleanPreferencesKey("widgetUseAbsoluteTimeVirtueNextTruthEgg")
    val SHOW_NEXT_TRUTH_EGG_GOAL_AMOUNT = booleanPreferencesKey("widgetShowNextTruthEggGoalAmount")
    val OPEN_VIRTUE_COMPANION = booleanPreferencesKey("widgetOpenVirtueCompanion")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
}

class VirtueWidgetDataStore {
    suspend fun updateVirtueWidgetDataStore(
        context: Context,
        eid: String? = null,
        virtueInfo: VirtueInfo? = null,
        useAbsoluteTimeVirtueSilos: Boolean? = null,
        useAbsoluteTimeVirtueNextTruthEgg: Boolean? = null,
        showNextTruthEggGoalAmount: Boolean? = null,
        openVirtueCompanion: Boolean? = null,
        backgroundColor: Color? = null,
        textColor: Color? = null
    ) {
        val virtueWidgetIds = getVirtueWidgetIds(context)

        virtueWidgetIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                eid?.let { prefs[VirtueWidgetDataStorePreferencesKeys.EID] = it }
                virtueInfo?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.VIRTUE_INFO] =
                        Json.encodeToString(it)
                }
                useAbsoluteTimeVirtueSilos?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_VIRTUE_SILOS] = it
                }
                useAbsoluteTimeVirtueNextTruthEgg?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_VIRTUE_NEXT_TRUTH_EGG] =
                        it
                }
                showNextTruthEggGoalAmount?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.SHOW_NEXT_TRUTH_EGG_GOAL_AMOUNT] = it
                }
                openVirtueCompanion?.let {
                    prefs[VirtueWidgetDataStorePreferencesKeys.OPEN_VIRTUE_COMPANION] = it
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

    fun decodeVirtueInfo(virtueInfoJson: String): VirtueInfo {
        return try {
            Json.decodeFromString<VirtueInfo>(virtueInfoJson)
        } catch (e: Exception) {
            VirtueInfo()
        }
    }

    suspend fun clearAllData(context: Context) {
        val virtueWidgetIds = getVirtueWidgetIds(context)
        (virtueWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun getVirtueWidgetIds(context: Context): List<GlanceId> {
        val virtueWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueWidgetNormal::class.java)
        val virtueWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueWidgetLarge::class.java)

        return virtueWidgetNormalIds + virtueWidgetLargeIds
    }

    private suspend fun updateAllWidgets(context: Context) {
        VirtueWidgetNormal().updateAll(context)
        VirtueWidgetLarge().updateAll(context)
    }
}