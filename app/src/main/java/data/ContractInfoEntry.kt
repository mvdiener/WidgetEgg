package data

import kotlinx.serialization.Serializable

// Data class used to save contract information to preferences
@Serializable
data class ContractInfoEntry(
    var eggId: Int,
    var customEggId: String?,
    var contractName: String
)