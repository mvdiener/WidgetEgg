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

            try {
                if (prefEid.isNotBlank()) {
                    val contractInfo = fetchContractData(prefEid)
                    prefContractInfo = formatContractData(contractInfo)

                    preferences.saveContractInfo(prefContractInfo)

                    ContractWidgetDataStore().setEid(context, prefEid)
                    ContractWidgetDataStore().setContractInfo(context, prefContractInfo)

                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}