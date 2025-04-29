package widget.contracts

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.ContractInfoEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import widget.contracts.normal.ContractWidgetNormal

data object ContractWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val CONTRACT_INFO = stringPreferencesKey("widgetContractInfo")
    val USE_ABSOLUTE_TIME = booleanPreferencesKey("useAbsoluteTime")
    val USE_OFFLINE_TIME = booleanPreferencesKey("useOfflineTime")
    val OPEN_WASMEGG_DASHBOARD = booleanPreferencesKey("openWasmeggDashboard")
}

class ContractWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setContractInfo(context: Context, contractInfo: List<ContractInfoEntry>) {
        val contractString = Json.encodeToString(contractInfo)
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.CONTRACT_INFO] = contractString
                }
            }

        updateAllWidgets(context)
    }

    fun decodeContractInfo(contractJson: String): List<ContractInfoEntry> {
        return try {
            Json.decodeFromString<List<ContractInfoEntry>>(contractJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setUseAbsoluteTime(context: Context, useAbsoluteTime: Boolean) {
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] =
                        useAbsoluteTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setUseOfflineTime(context: Context, useOfflineTime: Boolean) {
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] =
                        useOfflineTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setOpenWasmeggDashboard(context: Context, openWasmeggDashboard: Boolean) {
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.OPEN_WASMEGG_DASHBOARD] =
                        openWasmeggDashboard
                }
            }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val contractWidgetNormalIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetNormal::class.java)
        (contractWidgetNormalIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun updateAllWidgets(context: Context) {
        ContractWidgetNormal().updateAll(context)
    }
}