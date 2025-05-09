package widget.contracts

import android.content.Context
import api.fetchContractData
import kotlinx.coroutines.runBlocking
import tools.utilities.formatContractData
import user.preferences.PreferencesDatastore

class ContractWidgetUpdater {
    fun updateContracts(context: Context) {
        runBlocking {
            val preferences = PreferencesDatastore(context)

            var prefContractInfo = preferences.getContractInfo()

            val prefEid = preferences.getEid()
            val prefUseAbsoluteTime = preferences.getUseAbsoluteTimeContract()
            val prefUseOfflineTime = preferences.getUseOfflineTime()
            val prefOpenWasmeggDashboard = preferences.getOpenWasmeggDashboard()

            try {
                if (prefEid.isNotBlank()) {
                    val contractInfo = fetchContractData(prefEid)
                    prefContractInfo = formatContractData(contractInfo)

                    preferences.saveContractInfo(prefContractInfo)

                    ContractWidgetDataStore().setEid(context, prefEid)
                    ContractWidgetDataStore().setContractInfo(context, prefContractInfo)
                    ContractWidgetDataStore().setUseAbsoluteTime(context, prefUseAbsoluteTime)
                    ContractWidgetDataStore().setUseOfflineTime(context, prefUseOfflineTime)
                    ContractWidgetDataStore().setOpenWasmeggDashboard(
                        context,
                        prefOpenWasmeggDashboard
                    )
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}