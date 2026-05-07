package tools.utilities

import data.Artifact
import data.Event
import data.PeriodicalsData
import data.Stone
import data.VIRTUE_DELIVERY_GOALS
import data.VIRTUE_EGGS
import data.VirtueFarmInfo
import data.VirtueInfo
import ei.Ei.ArtifactsDB.ActiveArtifactSet
import ei.Ei.ArtifactInventoryItem
import ei.Ei.Backup
import ei.Ei.FarmType
import java.util.UUID
import kotlin.math.max

fun formatVirtueData(backup: Backup, periodicalsData: PeriodicalsData?): VirtueInfo {
    val homeFarm = backup.farmsList.first { it.farmType == FarmType.HOME }
    val homeFarmIndex = backup.farmsList.indexOf(homeFarm)

    val baseSiloTimeSeconds = 3600.0
    val siloResearchLevel =
        backup.game.epicResearchList.find { research -> research.id == "silo_capacity" }?.level
            ?: 0
    val silosBuilt = homeFarm.silosOwned
    val maximumOfflineTimeSeconds = (baseSiloTimeSeconds + (siloResearchLevel * 360)) * silosBuilt

    val virtueArtifacts = getArtifacts(
        backup.artifactsDb.virtueAfxDb.activeArtifacts,
        backup.artifactsDb.virtueAfxDb.inventoryItemsList
    )
    val homeArtifacts = getArtifacts(
        backup.artifactsDb.activeArtifactSetsList.elementAtOrNull(homeFarmIndex),
        backup.artifactsDb.inventoryItemsList
    )

    return VirtueInfo(
        stateId = UUID.randomUUID().toString(),
        resets = backup.virtue.resets,
        shifts = backup.virtue.shiftCount,
        soulEggs = numberToString(backup.game.soulEggsD),
        totalTruthEggs = backup.virtue.eovEarnedList.sumOf { it }.toString(),
        eggId = homeFarm.eggType.number,
        population = numberToString(homeFarm.numChickens.toDouble()),
        lastBackupDate = homeFarm.lastStepTime,
        siloCount = silosBuilt,
        maximumOfflineTime = maximumOfflineTimeSeconds,
        virtueEquippedArtifacts = virtueArtifacts,
        homeEquippedArtifacts = homeArtifacts,
        dailyEvents = formatDailyEvents(periodicalsData),
        farms = formatVirtueFarms(backup.virtue)
    )
}

fun countTruthEggThresholdsPassed(delivered: Double): Int {
    var count = 0
    val maxTe = VIRTUE_DELIVERY_GOALS.size
    while (count < maxTe && delivered >= VIRTUE_DELIVERY_GOALS[count]) {
        count++
    }
    return count
}

private fun getArtifacts(
    activeArtifactList: ActiveArtifactSet?,
    inventory: List<ArtifactInventoryItem>
): List<Artifact> {
    if (activeArtifactList == null) return emptyList()

    val inventoryMap = inventory.associateBy { it.itemId }

    return activeArtifactList.slotsList.mapNotNull { slot ->
        val foundArtifact = inventoryMap[slot.itemId] ?: return@mapNotNull null
        val stones = foundArtifact.artifact.stonesList.map { stone ->
            Stone(
                name = stone.name.number,
                level = stone.level.number
            )
        }

        Artifact(
            name = foundArtifact.artifact.spec.name.number,
            rarity = foundArtifact.artifact.spec.rarity.number,
            level = foundArtifact.artifact.spec.level.number,
            stones = stones
        )
    }
}

private fun formatDailyEvents(periodicalsData: PeriodicalsData?): List<Event> {
    return periodicalsData?.dailyEvents?.map { event ->
        Event(
            type = event.type,
            multiplier = event.multiplier,
            isUltra = event.ccOnly
        )
    } ?: emptyList()
}

private fun formatVirtueFarms(virtue: Backup.Virtue): List<VirtueFarmInfo> {
    return VIRTUE_EGGS.mapIndexed { index, egg ->
        val eggsDelivered = virtue.eggsDeliveredList.elementAtOrNull(index) ?: 0.0
        val pendingTruthEggs = countTruthEggThresholdsPassed(eggsDelivered)
        val truthEggs = virtue.eovEarnedList.elementAtOrNull(index) ?: 0

        VirtueFarmInfo(
            eggId = egg.number,
            truthEggs = truthEggs,
            pendingTruthEggs = max(0, pendingTruthEggs - truthEggs),
            eggsDelivered = eggsDelivered
        )
    }
}