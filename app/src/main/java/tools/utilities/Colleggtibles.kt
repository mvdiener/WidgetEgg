package tools.utilities

import android.content.Context
import api.downloadImageBytes
import data.CustomEggInfoEntry
import data.FARM_SIZE_TIERS
import data.PeriodicalsData
import ei.Ei.Backup
import ei.Ei.GameModifier.GameDimension
import java.io.File
import java.io.FileOutputStream

fun formatCustomEggs(periodicalsData: PeriodicalsData?): List<CustomEggInfoEntry> {
    return periodicalsData?.customEggs?.map { egg ->
        val buffs = egg.buffsList.map { buff -> buff.value }.sorted()
        CustomEggInfoEntry(
            name = egg.identifier,
            buffType = egg.buffsList.firstOrNull()?.dimension ?: GameDimension.INVALID,
            buffs = buffs
        )
    } ?: emptyList()
}

// Gets the total possible habs multiplier, rather than what the user has achieved
fun getHabsColleggtiblesMultiplier(periodicalsData: PeriodicalsData?): Double {
    val customEggs = formatCustomEggs(periodicalsData)
    return customEggs.filter { egg -> egg.buffType == GameDimension.HAB_CAPACITY && egg.buffs.max() > 0.0 }
        .fold(1.0) { total, egg ->
            total * egg.buffs.max()
        }
}

// Gets the total ihr multiplier that the user has achieved
fun getIhrColleggtiblesMultiplier(backup: Backup, periodicalsData: PeriodicalsData?): Double {
    if (periodicalsData == null) return 1.0

    val customEggs =
        formatCustomEggs(periodicalsData).filter { egg -> egg.buffType == GameDimension.INTERNAL_HATCHERY_RATE }
    val contracts = backup.contracts.archiveList + backup.contracts.contractsList

    return customEggs.fold(1.0) { total, egg ->
        val maxPop = contracts.filter { it.contract.identifier == egg.name }
            .maxOfOrNull { it.maxFarmSizeReached } ?: 0.0

        val reachedTierIndex = FARM_SIZE_TIERS.indexOfLast { maxPop >= it }

        if (reachedTierIndex == -1) return@fold total

        val eggMultiplier = egg.buffs.getOrElse(reachedTierIndex) { 1.0 }
        total * eggMultiplier
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