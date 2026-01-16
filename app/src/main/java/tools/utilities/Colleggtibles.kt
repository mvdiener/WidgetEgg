package tools.utilities

import data.Buff
import data.CustomEggInfoEntry
import data.PeriodicalsData
import ei.Ei.GameModifier.GameDimension


fun formatCustomEggs(periodicalsData: PeriodicalsData): List<CustomEggInfoEntry> {
    val customEggs = periodicalsData.customEggs

    var formattedCustomEggs: List<CustomEggInfoEntry> = emptyList()

    customEggs.forEach { egg ->
        val maxBuff = egg.buffsList.maxByOrNull { buff -> buff.value }
        formattedCustomEggs = formattedCustomEggs.plus(
            CustomEggInfoEntry(
                name = egg.identifier,
                imageUrl = egg.icon.url,
                buff = Buff(
                    type = maxBuff?.dimension ?: GameDimension.INVALID,
                    maxValue = maxBuff?.value ?: 0.0
                )
            )
        )
    }

    return formattedCustomEggs
}