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
    suspend fun updateMissionWidgetDataStore(
        context: Context,
        eid: String? = null,
        missionInfo: List<MissionInfoEntry>? = null,
        virtueMissionInfo: List<MissionInfoEntry>? = null,
        tankInfo: TankInfo? = null,
        virtueTankInfo: TankInfo? = null,
        useAbsoluteTime: Boolean? = null,
        useAbsoluteTimePlusDay: Boolean? = null,
        targetArtifactNormalWidget: Boolean? = null,
        targetArtifactLargeWidget: Boolean? = null,
        showFuelingShip: Boolean? = null,
        showTankLevels: Boolean? = null,
        useSliderCapacity: Boolean? = null,
        openEggInc: Boolean? = null,
        backgroundColor: Color? = null,
        textColor: Color? = null
    ) {
        val allIds = getMissionWidgetIds(context) + getVirtueMissionWidgetIds(context)
        allIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                eid?.let { prefs[MissionWidgetDataStorePreferencesKeys.EID] = it }
                missionInfo?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] =
                        Json.encodeToString(it)
                }
                virtueMissionInfo?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.VIRTUE_MISSION_INFO] =
                        Json.encodeToString(it)
                }
                tankInfo?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.TANK_INFO] = Json.encodeToString(it)
                }
                virtueTankInfo?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.VIRTUE_TANK_INFO] =
                        Json.encodeToString(it)
                }
                useAbsoluteTime?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] = it
                }
                useAbsoluteTimePlusDay?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_PLUS_DAY] = it
                }
                targetArtifactNormalWidget?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_NORMAL_WIDGET] = it
                }
                targetArtifactLargeWidget?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_LARGE_WIDGET] = it
                }
                showFuelingShip?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.SHOW_FUELING_SHIP] = it
                }
                showTankLevels?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.SHOW_TANK_LEVELS] = it
                }
                useSliderCapacity?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.USE_SLIDER_CAPACITY] = it
                }
                openEggInc?.let { prefs[MissionWidgetDataStorePreferencesKeys.OPEN_EGG_INC] = it }
                backgroundColor?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        it.toArgb()
                }
                textColor?.let {
                    prefs[MissionWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] = it.toArgb()
                }
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

    fun decodeTankInfo(tankInfoJson: String): TankInfo {
        return try {
            Json.decodeFromString<TankInfo>(tankInfoJson)
        } catch (e: Exception) {
            TankInfo()
        }
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