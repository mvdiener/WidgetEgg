package user.preferences

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import data.CalendarEntry
import data.ContractInfoEntry
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.MissionInfoEntry
import data.StatsInfo
import data.TankInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eiUserPrefs")

class PreferencesDatastore(context: Context) {
    private val dataStore = context.dataStore

    private companion object {
        private val EID = stringPreferencesKey("eid")
        private val EI_USER_NAME = stringPreferencesKey("eiUserName")
        private val MISSION_INFO = stringPreferencesKey("missionInfo")
        private val TANK_INFO = stringPreferencesKey("tankInfo")
        private val USE_ABSOLUTE_TIME = booleanPreferencesKey("useAbsoluteTime")
        private val USE_ABSOLUTE_TIME_PLUS_DAY = booleanPreferencesKey("useAbsoluteTimePlusDay")
        private val TARGET_ARTIFACT_NORMAL_WIDGET =
            booleanPreferencesKey("targetArtifactNormalWidget")
        private val TARGET_ARTIFACT_LARGE_WIDGET =
            booleanPreferencesKey("targetArtifactLargeWidget")
        private val SHOW_FUELING_SHIP = booleanPreferencesKey("showFuelingShip")
        private val SHOW_TANK_LEVELS = booleanPreferencesKey("showTankLevels")
        private val USE_SLIDER_CAPACITY = booleanPreferencesKey("useSliderCapacity")
        private val OPEN_EGG_INC = booleanPreferencesKey("openEggInc")
        private val SCHEDULE_EVENTS = booleanPreferencesKey("scheduleEvents")
        private val SELECTED_CALENDAR = stringPreferencesKey("selectedCalendar")
        private val CONTRACT_INFO = stringPreferencesKey("contractInfo")
        private val USE_ABSOLUTE_TIME_CONTRACT = booleanPreferencesKey("useAbsoluteTimeContract")
        private val USE_OFFLINE_TIME = booleanPreferencesKey("useOfflineTime")
        private val OPEN_WASMEGG_DASHBOARD = booleanPreferencesKey("openWasmeggDashboard")
        private val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
        private val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
        private val STATS_INFO = stringPreferencesKey("statsInfo")
        private val SHOW_COMMUNITY_BADGES = booleanPreferencesKey("showCommunityBadges")
        private val ALL_KEYS = listOf(
            EID,
            EI_USER_NAME,
            MISSION_INFO,
            TANK_INFO,
            USE_ABSOLUTE_TIME,
            USE_ABSOLUTE_TIME_PLUS_DAY,
            TARGET_ARTIFACT_NORMAL_WIDGET,
            TARGET_ARTIFACT_LARGE_WIDGET,
            SHOW_FUELING_SHIP,
            SHOW_TANK_LEVELS,
            USE_SLIDER_CAPACITY,
            OPEN_EGG_INC,
            SCHEDULE_EVENTS,
            SELECTED_CALENDAR,
            CONTRACT_INFO,
            USE_ABSOLUTE_TIME_CONTRACT,
            USE_OFFLINE_TIME,
            OPEN_WASMEGG_DASHBOARD,
            WIDGET_BACKGROUND_COLOR,
            WIDGET_TEXT_COLOR,
            STATS_INFO,
            SHOW_COMMUNITY_BADGES
        )
    }

    suspend fun getEid() = dataStore.data.map {
        it[EID] ?: ""
    }.first()

    suspend fun saveEid(eid: String) {
        dataStore.edit {
            it[EID] = eid
        }
    }

    suspend fun getEiUserName() = dataStore.data.map {
        it[EI_USER_NAME] ?: ""
    }.first()

    suspend fun saveEiUserName(userName: String) {
        dataStore.edit {
            it[EI_USER_NAME] = userName
        }
    }

    suspend fun getMissionInfo(): List<MissionInfoEntry> {
        return dataStore.data.map {
            it[MISSION_INFO]?.let { missionJson ->
                try {
                    Json.decodeFromString<List<MissionInfoEntry>>(missionJson)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }.first()
    }

    suspend fun saveMissionInfo(missionInfo: List<MissionInfoEntry>) {
        dataStore.edit {
            it[MISSION_INFO] = Json.encodeToString(missionInfo)
        }
    }

    suspend fun getTankInfo(): TankInfo {
        return dataStore.data.map {
            it[TANK_INFO]?.let { tankJson ->
                try {
                    Json.decodeFromString<TankInfo>(tankJson)
                } catch (e: Exception) {
                    TankInfo()
                }
            } ?: TankInfo()
        }.first()
    }

    suspend fun saveTankInfo(tankInfo: TankInfo) {
        dataStore.edit {
            it[TANK_INFO] = Json.encodeToString(tankInfo)
        }
    }

    suspend fun getUseAbsoluteTimeMission() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME] == true
    }.first()

    suspend fun saveUseAbsoluteTimeMission(useAbsoluteTimeMission: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME] = useAbsoluteTimeMission
        }
    }

    suspend fun getUseAbsoluteTimePlusDay() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME_PLUS_DAY] == true
    }.first()

    suspend fun saveUseAbsoluteTimePlusDay(useAbsoluteTimePlusDay: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME_PLUS_DAY] = useAbsoluteTimePlusDay
        }
    }

    suspend fun getTargetArtifactNormalWidget() = dataStore.data.map {
        it[TARGET_ARTIFACT_NORMAL_WIDGET] == true
    }.first()

    suspend fun saveTargetArtifactNormalWidget(targetArtifactNormalWidget: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_NORMAL_WIDGET] = targetArtifactNormalWidget
        }
    }

    suspend fun getTargetArtifactLargeWidget() = dataStore.data.map {
        it[TARGET_ARTIFACT_LARGE_WIDGET] == true
    }.first()

    suspend fun saveTargetArtifactLargeWidget(targetArtifactLargeWidget: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_LARGE_WIDGET] = targetArtifactLargeWidget
        }
    }

    suspend fun getShowFuelingShip() = dataStore.data.map {
        it[SHOW_FUELING_SHIP] == true
    }.first()

    suspend fun saveShowFuelingShip(showFuelingShip: Boolean) {
        dataStore.edit {
            it[SHOW_FUELING_SHIP] = showFuelingShip
        }
    }

    suspend fun getShowTankLevels() = dataStore.data.map {
        it[SHOW_TANK_LEVELS] == true
    }.first()

    suspend fun saveShowTankLevels(showTankLevels: Boolean) {
        dataStore.edit {
            it[SHOW_TANK_LEVELS] = showTankLevels
        }
    }

    suspend fun getUseSliderCapacity() = dataStore.data.map {
        it[USE_SLIDER_CAPACITY] == true
    }.first()

    suspend fun saveUseSliderCapacity(useSliderCapacity: Boolean) {
        dataStore.edit {
            it[USE_SLIDER_CAPACITY] = useSliderCapacity
        }
    }

    suspend fun getOpenEggInc() = dataStore.data.map {
        it[OPEN_EGG_INC] == true
    }.first()

    suspend fun saveOpenEggInc(openEggInc: Boolean) {
        dataStore.edit {
            it[OPEN_EGG_INC] = openEggInc
        }
    }

    suspend fun getScheduleEvents() = dataStore.data.map {
        it[SCHEDULE_EVENTS] == true
    }.first()

    suspend fun saveScheduleEvents(scheduleEvents: Boolean) {
        dataStore.edit {
            it[SCHEDULE_EVENTS] = scheduleEvents
        }
    }

    suspend fun getSelectedCalendar(): CalendarEntry {
        return dataStore.data.map {
            it[SELECTED_CALENDAR]?.let { calendarJson ->
                try {
                    Json.decodeFromString<CalendarEntry>(calendarJson)
                } catch (e: Exception) {
                    CalendarEntry()
                }
            } ?: CalendarEntry()
        }.first()
    }

    suspend fun saveSelectedCalendar(selectedCalendar: CalendarEntry) {
        dataStore.edit {
            it[SELECTED_CALENDAR] = Json.encodeToString(selectedCalendar)
        }
    }

    suspend fun getContractInfo(): List<ContractInfoEntry> {
        return dataStore.data.map {
            it[CONTRACT_INFO]?.let { contractJson ->
                try {
                    Json.decodeFromString<List<ContractInfoEntry>>(contractJson)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }.first()
    }

    suspend fun saveContractInfo(contractInfo: List<ContractInfoEntry>) {
        dataStore.edit {
            it[CONTRACT_INFO] = Json.encodeToString(contractInfo)
        }
    }

    suspend fun getUseAbsoluteTimeContract() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME_CONTRACT] == true
    }.first()

    suspend fun saveUseAbsoluteTimeContract(useAbsoluteTimeContract: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME_CONTRACT] = useAbsoluteTimeContract
        }
    }

    suspend fun getUseOfflineTime() = dataStore.data.map {
        it[USE_OFFLINE_TIME] == true
    }.first()

    suspend fun saveUseOfflineTime(useOfflineTime: Boolean) {
        dataStore.edit {
            it[USE_OFFLINE_TIME] = useOfflineTime
        }
    }

    suspend fun getOpenWasmeggDashboard() = dataStore.data.map {
        it[OPEN_WASMEGG_DASHBOARD] == true
    }.first()

    suspend fun saveOpenWasmeggDashboard(openWasmeggDashboard: Boolean) {
        dataStore.edit {
            it[OPEN_WASMEGG_DASHBOARD] = openWasmeggDashboard
        }
    }

    suspend fun getWidgetBackgroundColor() = dataStore.data.map {
        it[WIDGET_BACKGROUND_COLOR]?.let { colorInt -> Color(colorInt) }
            ?: DEFAULT_WIDGET_BACKGROUND_COLOR
    }.first()

    suspend fun saveWidgetBackgroundColor(widgetBackgroundColor: Color) {
        dataStore.edit {
            it[WIDGET_BACKGROUND_COLOR] = widgetBackgroundColor.toArgb()
        }
    }

    suspend fun getWidgetTextColor() = dataStore.data.map {
        it[WIDGET_TEXT_COLOR]?.let { colorInt -> Color(colorInt) } ?: DEFAULT_WIDGET_TEXT_COLOR
    }.first()

    suspend fun saveWidgetTextColor(widgetTextColor: Color) {
        dataStore.edit {
            it[WIDGET_TEXT_COLOR] = widgetTextColor.toArgb()
        }
    }

    suspend fun getStatsInfo(): StatsInfo {
        return dataStore.data.map {
            it[STATS_INFO]?.let { statsJson ->
                try {
                    Json.decodeFromString<StatsInfo>(statsJson)
                } catch (e: Exception) {
                    StatsInfo()
                }
            } ?: StatsInfo()
        }.first()
    }

    suspend fun saveStatsInfo(statsInfo: StatsInfo) {
        dataStore.edit {
            it[STATS_INFO] = Json.encodeToString(statsInfo)
        }
    }

    suspend fun getShowCommunityBadges() = dataStore.data.map {
        it[SHOW_COMMUNITY_BADGES] == true
    }.first()

    suspend fun saveShowCommunityBadges(showCommunityBadges: Boolean) {
        dataStore.edit {
            it[SHOW_COMMUNITY_BADGES] = showCommunityBadges
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            ALL_KEYS.forEach { key ->
                preferences.remove(key)
            }
        }
    }
}