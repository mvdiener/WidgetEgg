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
    val SHOW_FUELING_SHIP = booleanPreferencesKey("widgetShowFuelingShip")
    val OPEN_EGG_INC = booleanPreferencesKey("widgetOpenEggInc")
}

class MissionWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetMinimal::class.java)
        (missionWidgetNormalIds + missionWidgetMinimalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setMissionInfo(context: Context, missionInfo: List<MissionInfoEntry>) {
        val missionString = Json.encodeToString(missionInfo)
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetMinimal::class.java)
        (missionWidgetNormalIds + missionWidgetMinimalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] = missionString
                }
            }

        updateAllWidgets(context)
    }

    fun decodeMissionInfo(missionJson: String): List<MissionInfoEntry> {
        return try {
            Json.decodeFromString<List<MissionInfoEntry>>(missionJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setUseAbsoluteTime(context: Context, useAbsoluteTime: Boolean) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] = useAbsoluteTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTargetArtifactSmall(context: Context, showTargetArtifactSmall: Boolean) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_SMALL] =
                        showTargetArtifactSmall
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setShowFuelingShip(context: Context, showFuelingShip: Boolean) {
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.SHOW_FUELING_SHIP] =
                        showFuelingShip
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setOpenEggInc(context: Context, openEggInc: Boolean) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetMinimal::class.java)
        (missionWidgetNormalIds + missionWidgetMinimalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.OPEN_EGG_INC] =
                        openEggInc
                }
            }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetMinimal::class.java)
        (missionWidgetNormalIds + missionWidgetMinimalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun updateAllWidgets(context: Context) {
        MissionWidgetNormal().updateAll(context)
        MissionWidgetMinimal().updateAll(context)
    }
}