package widget.contracts

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
import data.ContractInfoEntry
import data.CustomEggInfoEntry
import data.PeriodicalsContractInfoEntry
import data.SeasonGradeAndGoals
import kotlinx.serialization.json.Json
import widget.contracts.active.ContractWidgetActive
import widget.contracts.large.ContractWidgetLarge

data object ContractWidgetDataStorePreferencesKeys {
    val EID = stringPreferencesKey("widgetEid")
    val CONTRACT_INFO = stringPreferencesKey("widgetContractInfo")
    val PERIODICALS_CONTRACT_INFO = stringPreferencesKey("widgetPeriodicalsContractInfo")
    val SEASON_INFO = stringPreferencesKey("widgetSeasonInfo")
    val USE_ABSOLUTE_TIME = booleanPreferencesKey("useAbsoluteTime")
    val USE_OFFLINE_TIME = booleanPreferencesKey("useOfflineTime")
    val SHOW_AVAILABLE_CONTRACTS = booleanPreferencesKey("showAvailableContracts")
    val SHOW_SEASON_INFO = booleanPreferencesKey("showSeasonInfo")
    val OPEN_WASMEGG_DASHBOARD = booleanPreferencesKey("openWasmeggDashboard")
    val WIDGET_BACKGROUND_COLOR = intPreferencesKey("widgetBackgroundColor")
    val WIDGET_TEXT_COLOR = intPreferencesKey("widgetTextColor")
    val CUSTOM_EGGS = stringPreferencesKey("widgetCustomEggs")
}

class ContractWidgetDataStore {
    suspend fun updateContractWidgetDataStore(
        context: Context,
        eid: String? = null,
        contractInfo: List<ContractInfoEntry>? = null,
        periodicalsContractInfo: List<PeriodicalsContractInfoEntry>? = null,
        seasonInfo: SeasonGradeAndGoals? = null,
        useAbsoluteTime: Boolean? = null,
        useOfflineTime: Boolean? = null,
        showAvailableContracts: Boolean? = null,
        showSeasonInfo: Boolean? = null,
        openWasmeggDashboard: Boolean? = null,
        backgroundColor: Color? = null,
        textColor: Color? = null,
        customEggs: List<CustomEggInfoEntry>? = null
    ) {
        val contractWidgetIds = getContractWidgetIds(context)

        contractWidgetIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                eid?.let { prefs[ContractWidgetDataStorePreferencesKeys.EID] = it }
                contractInfo?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.CONTRACT_INFO] =
                        Json.encodeToString(it)
                }
                periodicalsContractInfo?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.PERIODICALS_CONTRACT_INFO] =
                        Json.encodeToString(it)
                }
                seasonInfo?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.SEASON_INFO] =
                        Json.encodeToString(it)
                }
                useAbsoluteTime?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] = it
                }
                useOfflineTime?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] = it
                }
                showAvailableContracts?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.SHOW_AVAILABLE_CONTRACTS] = it
                }
                showSeasonInfo?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.SHOW_SEASON_INFO] = it
                }
                openWasmeggDashboard?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.OPEN_WASMEGG_DASHBOARD] = it
                }
                backgroundColor?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR] =
                        it.toArgb()
                }
                textColor?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR] = it.toArgb()
                }
                customEggs?.let {
                    prefs[ContractWidgetDataStorePreferencesKeys.CUSTOM_EGGS] =
                        Json.encodeToString(it)
                }
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

    fun decodePeriodicalsContractInfo(contractJson: String): List<PeriodicalsContractInfoEntry> {
        return try {
            Json.decodeFromString<List<PeriodicalsContractInfoEntry>>(contractJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun decodeSeasonInfo(seasonInfoJson: String): SeasonGradeAndGoals {
        return try {
            Json.decodeFromString<SeasonGradeAndGoals>(seasonInfoJson)
        } catch (e: Exception) {
            SeasonGradeAndGoals()
        }
    }

    fun decodeCustomEggs(customEggsJson: String): List<CustomEggInfoEntry> {
        return try {
            Json.decodeFromString<List<CustomEggInfoEntry>>(customEggsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clearAllData(context: Context) {
        val contractWidgetIds = getContractWidgetIds(context)
        (contractWidgetIds)
            .forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.clear()
                }
            }

        updateAllWidgets(context)
    }

    private suspend fun getContractWidgetIds(context: Context): List<GlanceId> {
        val contractWidgetActiveIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetActive::class.java)
        val contractWidgetLargeIds =
            GlanceAppWidgetManager(context).getGlanceIds(ContractWidgetLarge::class.java)
        return contractWidgetActiveIds + contractWidgetLargeIds
    }

    private suspend fun updateAllWidgets(context: Context) {
        ContractWidgetActive().updateAll(context)
        ContractWidgetLarge().updateAll(context)
    }
}