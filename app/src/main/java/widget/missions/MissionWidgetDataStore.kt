package widget.missions

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
import data.MissionInfoEntry
import data.TankInfo
import kotlinx.serialization.json.Json
import widget.missions.large.MissionWidgetLarge
import widget.missions.large.VirtueMissionWidgetLarge
import widget.missions.minimal.MissionWidgetMinimal
import widget.missions.minimal.VirtueMissionWidgetMinimal
import widget.missions.normal.MissionWidgetNormal
import widget.missions.normal.VirtueMissionWidgetNormal

data object MissionWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val MISSION_INFO = stringPreferencesKey("widgetMissionInfo")
    val VIRTUE_MISSION_INFO = stringPreferencesKey("widgetVirtueMissionInfo")
    val TANK_INFO = stringPreferencesKey("widgetTankInfo")
    val VIRTUE_TANK_INFO = stringPreferencesKey("widgetVirtueTankInfo")
    val USE_ABSOLUTE_TIME = booleanPreferencesKey("widgetUseAbsoluteTime")
    val USE_ABSOLUTE_TIME_PLUS_DAY = booleanPreferencesKey("widgetUseAbsoluteTimePlusDay")
    val TARGET_ARTIFACT_NORMAL_WIDGET = booleanPreferencesKey("widgetNormalTargetArtifact")
    val TARGET_ARTIFACT_LARGE_WIDGET = booleanPreferencesKey("widgetLargeTargetArtifact")
    val SHOW_FUELING_SHIP = booleanPreferencesKey("widgetShowFuelingShip")
    val SHOW_TANK_LEVELS = booleanPreferencesKey("widgetShowTankLevels")
    val USE_SLIDER_CAPACITY = booleanPreferencesKey("widgetUseSliderCapacity")
    val OPEN_EGG_INC = booleanPreferencesKey("widgetOpenEggInc")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
}

class MissionWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        val missionWidgetIds = getMissionWidgetIds(context)
        val virtueMissionWidgetIds = getVirtueMissionWidgetIds(context)
        (missionWidgetIds + virtueMissionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setMissionInfo(context: Context, missionInfo: List<MissionInfoEntry>) {
        val missionString = Json.encodeToString(missionInfo)
        val missionWidgetIds = getMissionWidgetIds(context)
        (missionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] = missionString
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setVirtueMissionInfo(context: Context, missionInfo: List<MissionInfoEntry>) {
        val missionString = Json.encodeToString(missionInfo)
        val virtueMissionWidgetIds = getVirtueMissionWidgetIds(context)
        (virtueMissionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.VIRTUE_MISSION_INFO] = missionString
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

    suspend fun setTankInfo(context: Context, tankInfo: TankInfo) {
        val tankInfoString = Json.encodeToString(tankInfo)
        GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.TANK_INFO] = tankInfoString
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setVirtueTankInfo(context: Context, tankInfo: TankInfo) {
        val tankInfoString = Json.encodeToString(tankInfo)
        GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.VIRTUE_TANK_INFO] = tankInfoString
                }
            }

        updateAllWidgets(context)
    }

    fun decodeTankInfo(tankInfoJson: String): TankInfo {
        return try {
            Json.decodeFromString<TankInfo>(tankInfoJson)
        } catch (e: Exception) {
            TankInfo()
        }
    }

    suspend fun setUseAbsoluteTime(context: Context, useAbsoluteTime: Boolean) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetNormalIds + missionWidgetLargeIds + virtueMissionWidgetNormalIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] = useAbsoluteTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setUseAbsoluteTimePlusDay(context: Context, useAbsoluteTimePlusDay: Boolean) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetNormalIds + missionWidgetLargeIds + virtueMissionWidgetNormalIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_PLUS_DAY] =
                        useAbsoluteTimePlusDay
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setOpenEggInc(context: Context, openEggInc: Boolean) {
        val missionWidgetIds = getMissionWidgetIds(context)
        val virtueMissionWidgetIds = getVirtueMissionWidgetIds(context)
        (missionWidgetIds + virtueMissionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.OPEN_EGG_INC] =
                        openEggInc
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTargetArtifactNormalWidget(
        context: Context,
        showTargetArtifactNormalWidget: Boolean
    ) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        (missionWidgetNormalIds + virtueMissionWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_NORMAL_WIDGET] =
                        showTargetArtifactNormalWidget
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setShowFuelingShip(context: Context, showFuelingShip: Boolean) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        (missionWidgetNormalIds + virtueMissionWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.SHOW_FUELING_SHIP] =
                        showFuelingShip
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTargetArtifactLargeWidget(
        context: Context,
        showTargetArtifactLargeWidget: Boolean
    ) {
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetLargeIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_LARGE_WIDGET] =
                        showTargetArtifactLargeWidget
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setShowTankLevels(context: Context, showTankLevels: Boolean) {
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetLargeIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.SHOW_TANK_LEVELS] =
                        showTankLevels
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setUseSliderCapacity(context: Context, useSliderCapacity: Boolean) {
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetLargeIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_SLIDER_CAPACITY] =
                        useSliderCapacity
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setBackgroundColor(context: Context, backgroundColor: Color) {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        (missionWidgetNormalIds + missionWidgetLargeIds + virtueMissionWidgetNormalIds + virtueMissionWidgetLargeIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        backgroundColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTextColor(context: Context, textColor: Color) {
        val missionWidgetIds = getMissionWidgetIds(context)
        val virtueMissionWidgetIds = getVirtueMissionWidgetIds(context)
        (missionWidgetIds + virtueMissionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[MissionWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] =
                        textColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val missionWidgetIds = getMissionWidgetIds(context)
        val virtueMissionWidgetIds = getVirtueMissionWidgetIds(context)
        (missionWidgetIds + virtueMissionWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun getMissionWidgetIds(context: Context): List<GlanceId> {
        val missionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetNormal::class.java)
        val missionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetMinimal::class.java)
        val missionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(MissionWidgetLarge::class.java)
        return missionWidgetNormalIds + missionWidgetMinimalIds + missionWidgetLargeIds
    }

    private suspend fun getVirtueMissionWidgetIds(context: Context): List<GlanceId> {
        val virtueMissionWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetNormal::class.java)
        val virtueMissionWidgetMinimalIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetMinimal::class.java)
        val virtueMissionWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(VirtueMissionWidgetLarge::class.java)
        return virtueMissionWidgetNormalIds + virtueMissionWidgetMinimalIds + virtueMissionWidgetLargeIds
    }

    private suspend fun updateAllWidgets(context: Context) {
        MissionWidgetNormal().updateAll(context)
        MissionWidgetMinimal().updateAll(context)
        MissionWidgetLarge().updateAll(context)
        VirtueMissionWidgetNormal().updateAll(context)
        VirtueMissionWidgetMinimal().updateAll(context)
        VirtueMissionWidgetLarge().updateAll(context)
    }
}