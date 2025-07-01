package data

import kotlinx.serialization.Serializable

// Data class used to save backup stats to preferences
@Serializable
data class StatsInfoEntry(
    var hasProPermit: Boolean,
    var soulEggs: String,
    var prophecyEggs: String,
    var farmerRoleId: Int,
    var earningsBonus: String,
    var goldEggs: String,
    var tickets: String,
    var homeFarmEggId: Int,
    var homeFarmPopulation: String,
    var contractGrade: Int,
    var contractSeasonScore: String,
    var contractTotalScore: String,
    var shipsLaunched: String,
    var droneTakedowns: String
)