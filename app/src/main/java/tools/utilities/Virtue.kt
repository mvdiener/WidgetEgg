package tools.utilities

import data.PeriodicalsData
import data.VirtueInfo
import ei.Ei.Backup
import ei.Ei.FarmType
import java.util.UUID

fun formatVirtueData(backup: Backup, periodicalsData: PeriodicalsData?): VirtueInfo {
    val homeFarm = backup.farmsList.first { it.farmType == FarmType.HOME }

    return VirtueInfo(
        stateId = UUID.randomUUID().toString(),
        resets = backup.virtue.resets,
        shifts = backup.virtue.shiftCount,
        soulEggs = numberToString(backup.game.soulEggsD),
        totalTruthEggs = backup.virtue.eovEarnedList.sumOf { it }.toString(),
        eggId = homeFarm.eggType.number,
        population = numberToString(homeFarm.numChickens.toDouble()),
    )
}