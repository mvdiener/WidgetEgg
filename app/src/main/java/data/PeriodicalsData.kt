package data

import ei.Ei.Contract
import ei.Ei.ContractSeasonInfo
import ei.Ei.CustomEgg

data class PeriodicalsData(
    val contracts: List<Contract>,
    val customEggs: List<CustomEgg>,
    val seasonInfo: ContractSeasonInfo
)