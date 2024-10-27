package user.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import data.MissionInfoEntry
import data.TankInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
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
            OPEN_EGG_INC
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

    suspend fun getUseAbsoluteTime() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME] ?: false
    }.first()

    suspend fun saveUseAbsoluteTime(useAbsoluteTime: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME] = useAbsoluteTime
        }
    }

    suspend fun getUseAbsoluteTimePlusDay() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME_PLUS_DAY] ?: false
    }.first()

    suspend fun saveUseAbsoluteTimePlusDay(useAbsoluteTimePlusDay: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME_PLUS_DAY] = useAbsoluteTimePlusDay
        }
    }

    suspend fun getTargetArtifactNormalWidget() = dataStore.data.map {
        it[TARGET_ARTIFACT_NORMAL_WIDGET] ?: false
    }.first()

    suspend fun saveTargetArtifactNormalWidget(targetArtifactNormalWidget: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_NORMAL_WIDGET] = targetArtifactNormalWidget
        }
    }

    suspend fun getTargetArtifactLargeWidget() = dataStore.data.map {
        it[TARGET_ARTIFACT_LARGE_WIDGET] ?: false
    }.first()

    suspend fun saveTargetArtifactLargeWidget(targetArtifactLargeWidget: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_LARGE_WIDGET] = targetArtifactLargeWidget
        }
    }

    suspend fun getShowFuelingShip() = dataStore.data.map {
        it[SHOW_FUELING_SHIP] ?: false
    }.first()

    suspend fun saveShowFuelingShip(showFuelingShip: Boolean) {
        dataStore.edit {
            it[SHOW_FUELING_SHIP] = showFuelingShip
        }
    }

    suspend fun getShowTankLevels() = dataStore.data.map {
        it[SHOW_TANK_LEVELS] ?: false
    }.first()

    suspend fun saveShowTankLevels(showTankLevels: Boolean) {
        dataStore.edit {
            it[SHOW_TANK_LEVELS] = showTankLevels
        }
    }

    suspend fun getUseSliderCapacity() = dataStore.data.map {
        it[USE_SLIDER_CAPACITY] ?: false
    }.first()

    suspend fun saveUseSliderCapacity(useSliderCapacity: Boolean) {
        dataStore.edit {
            it[USE_SLIDER_CAPACITY] = useSliderCapacity
        }
    }

    suspend fun getOpenEggInc() = dataStore.data.map {
        it[OPEN_EGG_INC] ?: false
    }.first()

    suspend fun saveOpenEggInc(openEggInc: Boolean) {
        dataStore.edit {
            it[OPEN_EGG_INC] = openEggInc
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