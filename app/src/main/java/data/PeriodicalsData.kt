package data

import ei.Ei.Contract
import ei.Ei.ContractPlayerInfo
import ei.Ei.ContractSeasonInfo
import ei.Ei.CustomEgg

// Data class used as return object from api.fetchPeriodicalsData
data class PeriodicalsData(
    val contracts: List<Contract>,
    val contractPlayerInfo: ContractPlayerInfo,
    val customEggs: List<CustomEgg>,
    val seasonInfo: ContractSeasonInfo
)