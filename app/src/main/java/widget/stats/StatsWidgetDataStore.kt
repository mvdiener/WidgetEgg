package widget.stats

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.CustomEggInfoEntry
import data.StatsInfo
import kotlinx.serialization.json.Json
import widget.stats.normal.StatsWidgetNormal

data object StatsWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val EI_USER_NAME = stringPreferencesKey("widgetEiUserName")
    val STATS_INFO = stringPreferencesKey("widgetStatsInfo")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
    val SHOW_COMMUNITY_BADGES = booleanPreferencesKey("showCommunityBadges")
    val CUSTOM_EGGS = stringPreferencesKey("widgetCustomEggs")
}

class StatsWidgetDataStore {
    suspend fun updateStatsWidgetDataStore(
        context: Context,
        eid: String? = null,
        eiUserName: String? = null,
        statsInfo: StatsInfo? = null,
        backgroundColor: Color? = null,
        textColor: Color? = null,
        showCommunityBadges: Boolean? = null,
        customEggs: List<CustomEggInfoEntry>? = null
    ) {
        val statsWidgetIds =
            GlanceAppWidgetManager(context).getGlanceIds(StatsWidgetNormal::class.java)

        statsWidgetIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                eid?.let { prefs[StatsWidgetDataStorePreferencesKeys.EID] = it }
                eiUserName?.let { prefs[StatsWidgetDataStorePreferencesKeys.EI_USER_NAME] = it }
                statsInfo?.let {
                    prefs[StatsWidgetDataStorePreferencesKeys.STATS_INFO] = Json.encodeToString(it)
                }
                backgroundColor?.let {
                    prefs[StatsWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] = it.toArgb()
                }
                textColor?.let {
                    prefs[StatsWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] = it.toArgb()
                }
                showCommunityBadges?.let {
                    prefs[StatsWidgetDataStorePreferencesKeys.SHOW_COMMUNITY_BADGES] = it
                }
                customEggs?.let {
                    prefs[StatsWidgetDataStorePreferencesKeys.CUSTOM_EGGS] = Json.encodeToString(it)
                }
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

    fun decodeCustomEggs(customEggsJson: String): List<CustomEggInfoEntry> {
        return try {
            Json.decodeFromString<List<CustomEggInfoEntry>>(customEggsJson)
        } catch (e: Exception) {
            emptyList()
        }
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