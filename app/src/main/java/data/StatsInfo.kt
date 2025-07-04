package data

import kotlinx.serialization.Serializable

// Data class used to save backup stats to preferences
@Serializable
data class StatsInfo(
    var stateId: String = "",
    var hasProPermit: Boolean = false,
    var soulEggs: String = "",
    var prophecyEggs: String = "",
    var farmerRoleId: Int = 0,
    var earningsBonus: String = "",
    var goldEggs: String = "",
    var tickets: String = "",
    var homeFarmEggId: Int = 0,
    var homeFarmPopulation: String = "",
    var contractGrade: Int = 0,
    var contractSeasonScore: String = "",
    var contractTotalScore: String = "",
    var shipsLaunched: String = "",
    var droneTakedowns: String = "",
    var craftingLevel: Int = 0,
    var craftingXP: String = ""
)