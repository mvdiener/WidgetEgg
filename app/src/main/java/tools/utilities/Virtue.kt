package tools.utilities

import data.Artifact
import data.Event
import data.constants.IHR_RESEARCHES
import data.PeriodicalsData
import data.Stone
import data.constants.VIRTUE_DELIVERY_GOALS
import data.constants.VIRTUE_EGGS
import data.VirtueFarmInfo
import data.VirtueInfo
import data.constants.IHR_ARTIFACTS
import data.constants.IHR_STONES
import ei.Ei.ArtifactsDB.ActiveArtifactSet
import ei.Ei.ArtifactInventoryItem
import ei.Ei.Backup
import ei.Ei.FarmType
import java.util.UUID
import kotlin.math.max
import java.time.Instant
import kotlin.math.pow

fun formatVirtueData(
    backup: Backup,
    periodicalsData: PeriodicalsData?
): VirtueInfo {
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

    val virtueFarms = formatVirtueFarms(backup.virtue)

    val isOnVirtue = homeFarm.eggType.number in VIRTUE_EGGS

    val (onlineHatcheryRate, offlineHatcheryRate) = if (isOnVirtue) {
        getInternalHatcheryRate(backup, periodicalsData, homeFarm, homeArtifacts)
    } else {
        Pair(0.0, 0.0)
    }

    return VirtueInfo(
        stateId = UUID.randomUUID().toString(),
        resets = backup.virtue.resets,
        shifts = backup.virtue.shiftCount,
        nextShiftCost = numberToString(getNextShiftCost(backup)),
        totalTruthEggs = backup.virtue.eovEarnedList.sumOf { it }.toString(),
        totalPendingTruthEggs = virtueFarms.sumOf { it.pendingTruthEggs }.toString(),
        eggId = homeFarm.eggType.number,
        population = numberToString(homeFarm.numChickens.toDouble()),
        lastBackupDate = homeFarm.lastStepTime,
        maximumOfflineTime = maximumOfflineTimeSeconds,
        isOnVirtue = isOnVirtue,
        virtueEquippedArtifacts = virtueArtifacts,
        homeEquippedArtifacts = homeArtifacts,
        dailyEvents = formatDailyEvents(periodicalsData),
        farms = virtueFarms
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

fun getRemainingSiloTime(lastBackupDate: Double, maximumOfflineTime: Double): String {
    val offlineTime = Instant.now().epochSecond.toDouble() - lastBackupDate
    val timeRemaining = maximumOfflineTime - offlineTime

    if (timeRemaining <= 0) return "Empty!"

    val hours = (timeRemaining / 3600).toInt()
    val minutes = ((timeRemaining % 3600) / 60).toInt()

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

//TODO: get correct image paths
fun getVirtueFunctionIconPath(eggId: Int): String {
    return when (eggId) {
        50 -> "eggs/egg_soul.png"
        51 -> "eggs/egg_of_prophecy.png"
        52 -> "eggs/egg_truth.png"
        53 -> "eggs/egg_truth.png"
        54 -> "eggs/egg_truth.png"
        else -> ""
    }
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
            eggId = egg,
            truthEggs = truthEggs,
            pendingTruthEggs = max(0, pendingTruthEggs - truthEggs),
            eggsDelivered = eggsDelivered
        )
    }
}

private fun getNextShiftCost(backup: Backup): Double {
    val soulEggs = backup.game.soulEggsD
    val shiftCount = backup.virtue.shiftCount.toDouble()

    val basisMultiplier = 0.02 * (shiftCount / 120.0).pow(3.0) + 0.0001
    val basis = soulEggs * basisMultiplier

    return 10.0.pow(11.0) + (0.6 * basis) + (0.4 * basis).pow(0.9)
}

private fun getInternalHatcheryRate(
    backup: Backup,
    periodicalsData: PeriodicalsData?,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Pair<Double, Double> {
    val (onlineMultiplier, offlineMultiplier, researchBaseRate) = getResearchIhrMultiplier(
        backup,
        homeFarm
    )
    if (onlineMultiplier == 0.0) return Pair(0.0, 0.0)

    val colleggtibleMultiplier = getIhrColleggtiblesMultiplier(backup, periodicalsData)
    val truthEggBonus = (1.1).pow(backup.virtue.eovEarnedList.sumOf { it })
    val artifactsMultiplier = getArtifactsMultiplier(virtueArtifacts, IHR_ARTIFACTS, IHR_STONES)

    val onlineRatePerHab =
        researchBaseRate * onlineMultiplier * colleggtibleMultiplier * artifactsMultiplier * truthEggBonus
    // Technically we should calculate the online rate based on the internal hatchery sharing ER
    // We are assuming people probably have their ER finished by the time they do virtue
    // So we have a hard coded 4x for all habs
    val onlineRate = 4 * onlineRatePerHab
    val offlineRate = onlineRate * offlineMultiplier


    return Pair(onlineRate, offlineRate)
}

private fun getResearchIhrMultiplier(
    backup: Backup,
    homeFarm: Backup.Simulation
): Triple<Double, Double, Double> {
    var baseRate = 0.0
    var onlineMultiplier = 1.0
    var offlineMultiplier = 1.0

    val commonResearch = homeFarm.commonResearchList
    val epicResearch = backup.game.epicResearchList
    IHR_RESEARCHES.forEach { research ->
        if (research.isMultiplicative) {
            val level = epicResearch.find { it.id == research.id }?.level ?: 0
            val multiplier = 1.0 + (level * research.perLevelValue)
            if (research.isOfflineOnly) {
                offlineMultiplier *= multiplier
            } else {
                onlineMultiplier *= multiplier
            }
        } else {
            val level = commonResearch.find { it.id == research.id }?.level ?: 0
            baseRate += level * research.perLevelValue
        }
    }

    return Triple(onlineMultiplier, offlineMultiplier, baseRate)
}

private fun getArtifactsMultiplier(
    equippedArtifacts: List<Artifact>,
    artifactSearchList: Array<Artifact>,
    stoneSearchList: Array<Stone>
): Double {
    var baseRate = 1.0

    equippedArtifacts.forEach { artifact ->
        val matchingArtifact = artifactSearchList.find {
            it.name == artifact.name && it.level == artifact.level && it.rarity == artifact.rarity
        }
        if (matchingArtifact != null) {
            baseRate *= (1.0 + matchingArtifact.effectValue!!)
        }

        artifact.stones.forEach { stone ->
            val matchingStone = stoneSearchList.find {
                it.name == stone.name && it.level == stone.level
            }
            if (matchingStone != null) {
                baseRate *= (1.0 + matchingStone.effectValue!!)
            }
        }
    }

    return baseRate
}