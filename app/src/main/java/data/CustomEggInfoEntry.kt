package data

import ei.Ei.GameModifier.GameDimension
import kotlinx.serialization.Serializable

// Data class used to save colleggtible information to preferences
@Serializable
data class CustomEggInfoEntry(
    var name: String,
    var imageUrl: String,
    var buff: Buff
)

@Serializable
data class Buff(
    var type: GameDimension,
    var maxValue: Double
)