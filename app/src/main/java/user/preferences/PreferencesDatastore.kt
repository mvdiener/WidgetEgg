package user.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import data.MissionInfoEntry
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
        private val USE_ABSOLUTE_TIME = booleanPreferencesKey("useAbsoluteTime")
        private val TARGET_ARTIFACT_SMALL = booleanPreferencesKey("targetArtifactSmall")
        private val TARGET_ARTIFACT_MEDIUM = booleanPreferencesKey("targetArtifactMedium")
        private val SHOW_FUELING_SHIP = booleanPreferencesKey("showFuelingShip")
        private val SHOW_TANK_LEVELS = booleanPreferencesKey("showTankLevels")
        private val ALL_KEYS = listOf(
            EID,
            EI_USER_NAME,
            MISSION_INFO,
            USE_ABSOLUTE_TIME,
            TARGET_ARTIFACT_SMALL,
            TARGET_ARTIFACT_MEDIUM,
            SHOW_FUELING_SHIP,
            SHOW_TANK_LEVELS
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

    suspend fun getUseAbsoluteTime() = dataStore.data.map {
        it[USE_ABSOLUTE_TIME] ?: false
    }.first()

    suspend fun saveUseAbsoluteTime(useAbsoluteTime: Boolean) {
        dataStore.edit {
            it[USE_ABSOLUTE_TIME] = useAbsoluteTime
        }
    }

    suspend fun getTargetArtifactSmall() = dataStore.data.map {
        it[TARGET_ARTIFACT_SMALL] ?: false
    }.first()

    suspend fun saveTargetArtifactSmall(targetArtifactSmall: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_SMALL] = targetArtifactSmall
        }
    }

    suspend fun getTargetArtifactMedium() = dataStore.data.map {
        it[TARGET_ARTIFACT_MEDIUM] ?: false
    }.first()

    suspend fun saveTargetArtifactMedium(targetArtifactMedium: Boolean) {
        dataStore.edit {
            it[TARGET_ARTIFACT_MEDIUM] = targetArtifactMedium
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

    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            ALL_KEYS.forEach { key ->
                preferences.remove(key)
            }
        }
    }
}