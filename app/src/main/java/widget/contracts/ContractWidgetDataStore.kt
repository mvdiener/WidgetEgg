package widget.contracts

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import data.ContractInfoEntry
import kotlinx.serialization.json.Json
import widget.contracts.active.ContractWidgetActive

data object ContractWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val CONTRACT_INFO = stringPreferencesKey("widgetContractInfo")
    val USE_ABSOLUTE_TIME = booleanPreferencesKey("useAbsoluteTime")
    val USE_OFFLINE_TIME = booleanPreferencesKey("useOfflineTime")
    val OPEN_WASMEGG_DASHBOARD = booleanPreferencesKey("openWasmeggDashboard")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
}

class ContractWidgetDataStore {
    suspend fun setEid(context: Context, eid: String) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.EID] = eid
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setContractInfo(context: Context, contractInfo: List<ContractInfoEntry>) {
        val contractString = Json.encodeToString(contractInfo)
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
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
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] =
                        useAbsoluteTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setUseOfflineTime(context: Context, useOfflineTime: Boolean) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] =
                        useOfflineTime
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setOpenWasmeggDashboard(context: Context, openWasmeggDashboard: Boolean) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.OPEN_WASMEGG_DASHBOARD] =
                        openWasmeggDashboard
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setBackgroundColor(context: Context, backgroundColor: Color) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        backgroundColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun setTextColor(context: Context, textColor: Color) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[ContractWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] =
                        textColor.toArgb()
                }
            }

        updateAllWidgets(context)
    }

    suspend fun clearAllData(context: Context) {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        (contractWidgetActiveIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun updateAllWidgets(context: Context) {
        ContractWidgetActive().updateAll(context)
    }
}