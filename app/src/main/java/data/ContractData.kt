package data

import ei.Ei.ContractCoopStatusResponse
import ei.Ei.LocalContract

// Data class used as return object from api.fetchContractData
data class ContractData(
    val contracts: List<LocalContract>,
    val contractStatuses: List<ContractCoopStatusResponse>
)
