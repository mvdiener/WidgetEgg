package tools.utilities

import android.content.Context
import api.downloadImageBytes
import data.CustomEggInfoEntry
import data.constants.FARM_SIZE_TIERS
import data.PeriodicalsData
import data.PlayerColleggtibleInfoEntry
import ei.Ei.Backup
import ei.Ei.GameModifier.GameDimension
import java.io.File
import java.io.FileOutputStream

fun formatCustomEggs(periodicalsData: PeriodicalsData?): List<CustomEggInfoEntry> {
    return periodicalsData?.customEggs?.map { egg ->
        val buffs = egg.buffsList.map { buff -> buff.value }
        CustomEggInfoEntry(
            name = egg.identifier,
            buffType = egg.buffsList.firstOrNull()?.dimension ?: GameDimension.INVALID,
            buffs = buffs
        )
    } ?: emptyList()
}

fun getPlayerColleggtibles(
    periodicalsData: PeriodicalsData?,
    backup: Backup,
    preferencesPlayerColleggtibles: List<PlayerColleggtibleInfoEntry>
): List<PlayerColleggtibleInfoEntry> {
    val customEggs = formatCustomEggs(periodicalsData)
    if (customEggs.isEmpty()) return preferencesPlayerColleggtibles

    if (preferencesPlayerColleggtibles.isEmpty()
        || customEggs.size != preferencesPlayerColleggtibles.size
        || preferencesPlayerColleggtibles.any { it.bestPossibleBuff != it.bestAchievedBuff }
    ) {
        try {

            val colleggtiblesPopList =
                backup.contracts.colleggtibleMaxFarmSizeReachedList

            return customEggs.mapNotNull { egg ->
                if (egg.buffType == GameDimension.INVALID) return@mapNotNull null

                val bestPossibleBuff = if (egg.buffs.any { it < 1.0 }) {
                    egg.buffs.minOrNull() ?: 1.0
                } else {
                    egg.buffs.maxOrNull() ?: 1.0
                }

                val maxPop =
                    colleggtiblesPopList.find { it.eggId == egg.name }?.maxFarmSizeReached ?: 0.0

                val reachedTierIndex = FARM_SIZE_TIERS.indexOfLast { maxPop >= it }
                if (reachedTierIndex == -1) return@mapNotNull null

                PlayerColleggtibleInfoEntry(
                    name = egg.name,
                    buffType = egg.buffType,
                    bestPossibleBuff = bestPossibleBuff,
                    bestAchievedBuff = egg.buffs.getOrElse(reachedTierIndex) { 1.0 }
                )
            }
        } catch (_: Exception) {
            return preferencesPlayerColleggtibles
        }
    }

    return preferencesPlayerColleggtibles
}

// Gets the total possible habs multiplier, rather than what the user has achieved
fun getHabsColleggtiblesMultiplier(periodicalsData: PeriodicalsData?): Double {
    val customEggs = formatCustomEggs(periodicalsData)
    return customEggs.filter { egg -> egg.buffType == GameDimension.HAB_CAPACITY && egg.buffs.max() > 0.0 }
        .fold(1.0) { total, egg ->
            total * egg.buffs.max()
        }
}

// Gets the total colleggtible multiplier that the user has achieved for the given colleggtible type
fun getColleggtiblesMultiplier(
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>,
    colleggtibleType: GameDimension
): Double {
    val customEggs = playerColleggtibleInfo.filter { it.buffType == colleggtibleType }
    return customEggs.fold(1.0) { total, egg ->
        total * egg.bestAchievedBuff
    }
}

suspend fun saveColleggtibleImagesToCache(periodicalsData: PeriodicalsData, context: Context) {
    val customEggs = periodicalsData.customEggs

    customEggs.forEach { egg ->
        val fileName = "egg_${egg.identifier}.png"
        val file = File(context.cacheDir, fileName)

        if (!file.exists() || file.length() == 0L) {
            try {
                val imageBytes = downloadImageBytes(egg.icon.url)

                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    val tempFile = File(context.cacheDir, "${fileName}.tmp")

                    FileOutputStream(tempFile).use { output ->
                        output.write(imageBytes)
                    }

                    if (tempFile.exists()) {
                        tempFile.renameTo(file)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}