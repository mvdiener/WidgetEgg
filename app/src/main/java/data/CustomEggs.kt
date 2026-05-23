package data

import ei.Ei.GameModifier.GameDimension

data class CustomEggInfoEntry(
    var name: String,
    var buffType: GameDimension,
    var buffs: List<Double>
)