package data

import ei.Ei.LocalContract

// Data class used as return object from api.fetchContractData
data class ContractData(
    val contracts: List<LocalContract>
)
