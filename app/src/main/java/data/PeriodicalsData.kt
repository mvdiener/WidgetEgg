package data

import ei.Ei.Contract
import ei.Ei.ContractSeasonInfo
import ei.Ei.CustomEgg
import ei.Ei.EggIncEvent

// Data class used as return object from api.fetchPeriodicalsData
data class PeriodicalsData(
    val contracts: List<Contract>,
    val customEggs: List<CustomEgg>,
    val seasonInfo: ContractSeasonInfo,
    val dailyEvents: List<EggIncEvent>
)