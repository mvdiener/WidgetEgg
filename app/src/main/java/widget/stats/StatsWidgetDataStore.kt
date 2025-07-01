package widget.stats

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.StatsInfo
import kotlinx.serialization.json.Json
import widget.stats.normal.StatsWidgetNormal

data object StatsWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val EI_USER_NAME = stringPreferencesKey("widgetEiUserName")
    val STATS_INFO = stringPreferencesKey("widgetStatsInfo")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
}

class StatsWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[StatsWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setEiUserName(context: Context, eiUserName: String) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[StatsWidgetDataStorePreferencesKeys.EI_USER_NAME] = eiUserName
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setStatsInfo(context: Context, statsInfo: StatsInfo) {
        val statsString = Json.encodeToString(statsInfo)
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[StatsWidgetDataStorePreferencesKeys.STATS_INFO] = statsString
                }
            }

        updateAllWidgets(context)
    }

    fun decodeStatsInfo(statsInfoJson: String): StatsInfo {
        return try {
            Json.decodeFromString<StatsInfo>(statsInfoJson)
        } catch (e: Exception) {
            StatsInfo()
        }
    }

    suspend fun setBackgroundColor(context: Context, backgroundColor: Color) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[StatsWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        backgroundColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTextColor(context: Context, textColor: Color) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[StatsWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] =
                        textColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)
        (statsWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun updateAllWidgets(context: Context) {
        StatsWidgetNormal().updateAll(context)
    }
}