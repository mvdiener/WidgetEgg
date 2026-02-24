package tools.utilities

import android.content.Context
import api.downloadImageBytes
import data.Buff
import data.CustomEggInfoEntry
import data.PeriodicalsData
import ei.Ei.GameModifier.GameDimension
import java.io.File
import java.io.FileOutputStream


fun formatCustomEggs(periodicalsData: PeriodicalsData): List<CustomEggInfoEntry> {
    return periodicalsData.customEggs.map { egg ->
        val maxBuff = egg.buffsList.maxByOrNull { buff -> buff.value }
        CustomEggInfoEntry(
            name = egg.identifier,
            buff = Buff(
                type = maxBuff?.dimension ?: GameDimension.INVALID,
                maxValue = maxBuff?.value ?: 0.0
            )
        )
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