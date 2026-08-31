package data

import ei.Ei.GameModifier.GameDimension
import kotlinx.serialization.Serializable

data class CustomEggInfoEntry(
    var name: String,
    var buffType: GameDimension,
    var buffs: List<Double>
)

@Serializable
data class PlayerColleggtibleInfoEntry(
    var name: String,
    var buffType: GameDimension,
    var bestPossibleBuff: Double,
    var bestAchievedBuff: Double
)