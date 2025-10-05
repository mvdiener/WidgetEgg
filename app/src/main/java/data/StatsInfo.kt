package data

import kotlinx.serialization.Serializable

// Data class used to save backup stats to preferences
@Serializable
data class StatsInfo(
    var stateId: String = "",
    var hasProPermit: Boolean = false,
    var soulEggs: String = "",
    var prophecyEggs: String = "",
    var truthEggs: String = "",
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
    var craftingXP: String = "",
    var badges: Badges = Badges()
)

@Serializable
data class Badges(
    var hasAlc: Boolean = false,
    var hasAsc: Boolean = false,
    var hasCraftingLegend: Boolean = false,
    var hasEnd: Boolean = false,
    var hasNah: Boolean = false,
    var hasFed: Boolean = false,
    var hasAllShells: Boolean = false,
    var hasZlc: Boolean = false
)