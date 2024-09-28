package widget

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.MissionInfoEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data object MissionWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val MISSION_INFO = stringPreferencesKey("widgetMissionInfo")
    val USE_ABSOLUTE_TIME = booleanPreferencesKey("widgetUseAbsoluteTime")
    val TARGET_ARTIFACT_SMALL = booleanPreferencesKey("widgetTargetArtifactSmall")
}

class MissionWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidget::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        MissionWidget().updateAll(context)
    }

    suspend fun setMissionInfo(context: Context, missionInfo: List<MissionInfoEntry>) {
        val missionString = Json.encodeToString(missionInfo)
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidget::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] = missionString
                }
            }

        MissionWidget().updateAll(context)
    }

    fun decodeMissionInfo(missionJson: String): List<MissionInfoEntry> {
        return try {
            Json.decodeFromString<List<MissionInfoEntry>>(missionJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setUseAbsoluteTime(context: Context, useAbsoluteTime: Boolean) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidget::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] = useAbsoluteTime
                }
            }

        MissionWidget().updateAll(context)
    }

    suspend fun setTargetArtifactSmall(context: Context, showTargetArtifactSmall: Boolean) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidget::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_SMALL] =
                        showTargetArtifactSmall
                }
            }

        MissionWidget().updateAll(context)
    }

    suspend fun clearAllData(context: Context) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidget::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        MissionWidget().updateAll(context)
    }
}