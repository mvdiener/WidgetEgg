package widget.contracts

import android.content.Context
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