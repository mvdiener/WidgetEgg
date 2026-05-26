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
import data.constants.HABS
import data.constants.HAB_SPACE_ARTIFACTS
import data.constants.HAB_SPACE_RESEARCHES
import data.constants.IHR_ARTIFACTS
import data.constants.IHR_STONES
import data.constants.LAY_RATE_ARTIFACTS
import data.constants.LAY_RATE_RESEARCHES
import data.constants.LAY_RATE_STONES
import data.constants.SHIPPING_CAPACITY_ARTIFACTS
import data.constants.SHIPPING_CAPACITY_RESEARCHES
import data.constants.SHIPPING_CAPACITY_STONES
import data.constants.VEHICLES
import ei.Ei.ArtifactsDB.ActiveArtifactSet
import ei.Ei.ArtifactInventoryItem
import ei.Ei.Backup
import ei.Ei.FarmType
import ei.Ei.GameModifier.GameDimension
import java.util.UUID
import kotlin.math.max
import java.time.Instant
import kotlin.math.ceil
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

    val eggLayingRate = if (isOnVirtue) {
        getEggLayingRate(backup, periodicalsData, homeFarm, homeArtifacts)
    } else {
        0.0
    }

    val shippingCapacity = if (isOnVirtue) {
        getShippingCapacity(backup, periodicalsData, homeFarm, homeArtifacts)
    } else {
        0.0
    }

    val habCapacity = if (isOnVirtue) {
        getHabCapacity(backup, periodicalsData, homeFarm, homeArtifacts)
    } else {
        0.0
    }

    return VirtueInfo(
        stateId = UUID.randomUUID().toString(),
        resets = backup.virtue.resets,
        shifts = backup.virtue.shiftCount,
        nextShiftCost = numberToString(getNextShiftCost(backup)),
        totalTruthEggs = backup.virtue.eovEarnedList.sumOf { it }.toString(),
        totalPendingTruthEggs = virtueFarms.sumOf { it.pendingTruthEggs }.toString(),
        onlineHatcheryRate = onlineHatcheryRate,
        offlineHatcheryRate = offlineHatcheryRate,
        habCapacity = habCapacity,
        eggLayingRate = eggLayingRate,
        shippingCapacity = shippingCapacity,
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
        val truthEggs = virtue.eovEarnedList.elementAtOrNull(index) ?: 0
        val pendingTruthEggs = countTruthEggThresholdsPassed(eggsDelivered) - truthEggs

        VirtueFarmInfo(
            eggId = egg,
            truthEggs = truthEggs,
            pendingTruthEggs = max(0, pendingTruthEggs),
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

    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(backup, periodicalsData, GameDimension.INTERNAL_HATCHERY_RATE)
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
            val researchLevel = epicResearch.find { it.id == research.id }?.level ?: 0
            val multiplier = 1.0 + (researchLevel * research.perLevelValue)
            if (research.isOfflineOnly) {
                offlineMultiplier *= multiplier
            } else {
                onlineMultiplier *= multiplier
            }
        } else {
            val researchLevel = commonResearch.find { it.id == research.id }?.level ?: 0
            baseRate += researchLevel * research.perLevelValue
        }
    }

    return Triple(onlineMultiplier, offlineMultiplier, baseRate)
}

private fun getEggLayingRate(
    backup: Backup,
    periodicalsData: PeriodicalsData?,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Double {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(backup, periodicalsData, GameDimension.EGG_LAYING_RATE)
    val artifactsMultiplier = getArtifactsMultiplier(
        virtueArtifacts, LAY_RATE_ARTIFACTS,
        LAY_RATE_STONES
    )
    val researchLayRate = getResearchLayRate(backup, homeFarm)

    return 60 * homeFarm.numChickens * researchLayRate * artifactsMultiplier * colleggtibleMultiplier
}

private fun getResearchLayRate(
    backup: Backup,
    homeFarm: Backup.Simulation
): Double {
    var baseRate = 1.0 / 30.0 // 1 egg per 30 seconds
    val commonResearch = homeFarm.commonResearchList
    val epicResearch = backup.game.epicResearchList

    LAY_RATE_RESEARCHES.forEach { research ->
        val researchLevel = if (research.isEpic) {
            epicResearch.find { it.id == research.id }?.level ?: 0
        } else {
            commonResearch.find { it.id == research.id }?.level ?: 0
        }

        baseRate *= 1.0 + (researchLevel * research.perLevelValue)
    }

    return baseRate
}

private fun getShippingCapacity(
    backup: Backup,
    periodicalsData: PeriodicalsData?,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Double {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(backup, periodicalsData, GameDimension.SHIPPING_CAPACITY)
    val artifactsMultiplier = getArtifactsMultiplier(
        virtueArtifacts, SHIPPING_CAPACITY_ARTIFACTS,
        SHIPPING_CAPACITY_STONES
    )
    val (universalMultiplier, hoverOnlyMultiplier, hyperloopOnlyMultiplier) = getResearchShippingCapacity(
        backup,
        homeFarm
    )

    val vehicles = homeFarm.vehiclesList
    val trainLengths = homeFarm.trainLengthList

    if (vehicles.size != trainLengths.size || vehicles.any { it !in 0..11 }) return 0.0

    return vehicles.mapIndexed { index, vehicleId ->
        var capacity = (VEHICLES.find { it.id == vehicleId }?.baseCapacity
            ?: 0.0) * universalMultiplier * artifactsMultiplier * colleggtibleMultiplier

        if (vehicleId >= 9) {
            capacity *= hoverOnlyMultiplier
        }

        if (vehicleId == 11) {
            val trainLength = trainLengths.getOrElse(index) { 1 }
            capacity *= (trainLength * hyperloopOnlyMultiplier)
        }

        capacity
    }.sum()
}

private fun getResearchShippingCapacity(
    backup: Backup,
    homeFarm: Backup.Simulation
): Triple<Double, Double, Double> {
    var universalMultiplier = 1.0
    var hoverOnlyMultiplier = 1.0
    var hyperloopOnlyMultiplier = 1.0
    val commonResearch = homeFarm.commonResearchList
    val epicResearch = backup.game.epicResearchList

    SHIPPING_CAPACITY_RESEARCHES.forEach { research ->
        val researchLevel = if (research.isEpic) {
            epicResearch.find { it.id == research.id }?.level ?: 0
        } else {
            commonResearch.find { it.id == research.id }?.level ?: 0
        }

        val multiplier = 1 + researchLevel * research.perLevelValue
        if (research.isHoverOnly) {
            hoverOnlyMultiplier *= multiplier
        } else if (research.isHyperloopOnly) {
            hyperloopOnlyMultiplier *= multiplier
        } else {
            universalMultiplier *= multiplier
        }
    }

    return Triple(universalMultiplier, hoverOnlyMultiplier, hyperloopOnlyMultiplier)
}

private fun getHabCapacity(
    backup: Backup,
    periodicalsData: PeriodicalsData?,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Double {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(backup, periodicalsData, GameDimension.HAB_CAPACITY)
    val artifactsMultiplier =
        getArtifactsMultiplier(virtueArtifacts, HAB_SPACE_ARTIFACTS, emptyArray())
    val (universalMultiplier, portalOnlyMultiplier) = getResearchHabCapacity(homeFarm)

    val habs = homeFarm.habsList

    return habs.map { hab ->
        if (hab == 19) return@map 0.0 // id 19 represents unpurchased hab
        var habSpace = HABS.find { it.id == hab }?.baseHabSpace ?: 0.0

        habSpace *= universalMultiplier * artifactsMultiplier * colleggtibleMultiplier

        if (hab >= 17) {
            habSpace *= portalOnlyMultiplier
        }

        ceil(habSpace)
    }.sum()
}

private fun getResearchHabCapacity(
    homeFarm: Backup.Simulation
): Pair<Double, Double> {
    var universalMultiplier = 1.0
    var portalOnlyMultiplier = 1.0
    val commonResearch = homeFarm.commonResearchList

    HAB_SPACE_RESEARCHES.forEach { research ->
        val researchLevel = commonResearch.find { it.id == research.id }?.level ?: 0
        val multiplier = 1.0 + (researchLevel * research.perLevelValue)

        if (research.isPortalHabsOnly) {
            portalOnlyMultiplier *= multiplier
        } else {
            universalMultiplier *= multiplier
        }
    }

    return Pair(universalMultiplier, portalOnlyMultiplier)
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