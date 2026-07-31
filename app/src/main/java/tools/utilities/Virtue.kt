package tools.utilities

import data.Artifact
import data.Event
import data.PeriodicalsContractInfoEntry
import data.constants.IHR_RESEARCHES
import data.PeriodicalsData
import data.PlayerColleggtibleInfoEntry
import data.Stone
import data.constants.VIRTUE_DELIVERY_GOALS
import data.constants.VIRTUE_EGGS
import data.VirtueFarmInfo
import data.VirtueInfo
import data.constants.ALL_EVENT_TYPES
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun formatVirtueData(
    backup: Backup,
    periodicalsData: PeriodicalsData?,
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>
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

    val isOnVirtue = homeFarm.eggType.number in VIRTUE_EGGS

    val (onlineHatcheryRate, offlineHatcheryRate) = if (isOnVirtue) {
        getInternalHatcheryRate(backup, playerColleggtibleInfo, homeFarm, virtueArtifacts)
    } else {
        Pair(0.0, 0.0)
    }

    val (activeVirtueFarmEggLayingRate, inactiveVirtueFarmEggLayingRate) = if (isOnVirtue) {
        getEggLayingRate(backup, playerColleggtibleInfo, homeFarm, virtueArtifacts)
    } else {
        Pair(0.0, 0.0)
    }

    val shippingCapacity = if (isOnVirtue) {
        getShippingCapacity(backup, playerColleggtibleInfo, homeFarm, virtueArtifacts)
    } else {
        0.0
    }

    val habCapacity = if (isOnVirtue) {
        getHabCapacity(backup, playerColleggtibleInfo, homeFarm, virtueArtifacts)
    } else {
        0.0
    }

    val virtueFarms = formatVirtueFarms(backup.virtue, inactiveVirtueFarmEggLayingRate)

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
        eggLayingRate = activeVirtueFarmEggLayingRate,
        shippingCapacity = shippingCapacity,
        eggId = homeFarm.eggType.number,
        population = homeFarm.numChickens.toDouble(),
        lastBackupDate = homeFarm.lastStepTime,
        maximumOfflineTime = maximumOfflineTimeSeconds,
        isOnVirtue = isOnVirtue,
        virtueEquippedArtifacts = virtueArtifacts,
        homeEquippedArtifacts = homeArtifacts,
        dailyEvents = formatDailyEvents(periodicalsData),
        farms = virtueFarms
    )
}

fun getRemainingSiloTime(
    lastBackupDate: Double,
    maximumOfflineTime: Double,
    useAbsoluteTime: Boolean,
    use24HrFormat: Boolean
): String {
    val offlineTime = Instant.now().epochSecond.toDouble() - lastBackupDate
    val timeRemaining = maximumOfflineTime - offlineTime

    if (timeRemaining <= 0) return "Empty!"

    val hours = (timeRemaining / 3600).toInt()
    val minutes = ((timeRemaining % 3600) / 60).toInt()

    return if (useAbsoluteTime) {
        val currentTime = LocalDateTime.now()
        val endingTime = currentTime.plusSeconds(timeRemaining.toLong())
        val extraTime = if (hours >= 24) "⁺¹" else ""
        val pattern = if (use24HrFormat) "HH:mm" else "h:mm a"

        endingTime.format(DateTimeFormatter.ofPattern(pattern)) + extraTime
    } else {
        when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}

fun getTimeRemainingToNextTruthEgg(
    virtueInfo: VirtueInfo,
    farm: VirtueFarmInfo,
    useAbsoluteTime: Boolean,
    use24HrFormat: Boolean
): String {
    val isOnActiveFarm = virtueInfo.eggId == farm.eggId
    val offlineHatcheryRate = virtueInfo.offlineHatcheryRate
    val habCapacity = virtueInfo.habCapacity
    val eggsDelivered = farm.eggsDelivered
    val eggLayingRate = 60 * if (isOnActiveFarm) {
        virtueInfo.eggLayingRate
    } else {
        farm.inactiveLayRate
    }
    val shippingCapacity = 60 * virtueInfo.shippingCapacity
    val timeElapsedSeconds = if (isOnActiveFarm) {
        val offlineTime = Instant.now().epochSecond.toDouble() - virtueInfo.lastBackupDate
        // Progress stops as soon as silos are empty
        min(offlineTime, virtueInfo.maximumOfflineTime)
    } else {
        0.0
    }
    val population = if (isOnActiveFarm) {
        max(1.0, virtueInfo.population)
    } else {
        1.0
    }

    val perChickenPerMinuteLayingRate = eggLayingRate / population
    val effectivePopulationCap = shippingCapacity / perChickenPerMinuteLayingRate
    val maxPopulation = min(habCapacity, effectivePopulationCap)
    val startingEggLayingRate = if (isOnActiveFarm) {
        min(eggLayingRate, shippingCapacity)
    } else {
        perChickenPerMinuteLayingRate
    }
    val offlineEggsDelivered = if (isOnActiveFarm) {
        getVirtueOfflineEggsDelivered(virtueInfo, eggsDelivered)
    } else {
        eggsDelivered
    }
    val targetEggAmount =
        getNextTruthEggThreshold(offlineEggsDelivered)
    if (targetEggAmount == 0.0) return "Winner!"

    val targetEggsRemaining = targetEggAmount - eggsDelivered

    // No population growth possible - either no IHR or already at max capacity
    if (offlineHatcheryRate == 0.0 || population >= maxPopulation) {
        val remainingSeconds =
            ((60 * targetEggsRemaining) / startingEggLayingRate) - timeElapsedSeconds
        return formatTimeText(remainingSeconds, useAbsoluteTime, use24HrFormat)
    }

    var timeToTarget =
        (sqrt(
            population.pow(2) + (2 * offlineHatcheryRate * population * targetEggsRemaining) / startingEggLayingRate
        ) - population) / offlineHatcheryRate
    if (timeToTarget > (maxPopulation - population) / offlineHatcheryRate) {
        timeToTarget =
            ((population * targetEggsRemaining) / startingEggLayingRate +
                    ((maxPopulation - population) * (maxPopulation - population)) / (2 * offlineHatcheryRate)) /
                    maxPopulation
    }
    val remainingSeconds = 60 * timeToTarget - timeElapsedSeconds
    return formatTimeText(remainingSeconds, useAbsoluteTime, use24HrFormat)
}

fun getTruthEggPercentComplete(delivered: Double): Float {
    val nextTEThreshold = getNextTruthEggThreshold(delivered)
    if (nextTEThreshold == 0.0) return 1.0f
    val previousTEThreshold = getPreviousTruthEggThreshold(delivered)

    val totalNeeded = nextTEThreshold - previousTEThreshold
    return ((delivered - previousTEThreshold) / totalNeeded).toFloat()
}

fun getOfflineTruthEggPercentComplete(virtueInfo: VirtueInfo, delivered: Double): Float {
    val offlineDelivered = getVirtueOfflineEggsDelivered(virtueInfo, delivered)
    val nextTEThreshold = getNextTruthEggThreshold(delivered)
    if (nextTEThreshold == 0.0 || offlineDelivered >= nextTEThreshold) return 1.0f
    val previousTEThreshold = getPreviousTruthEggThreshold(delivered)

    val totalNeeded = nextTEThreshold - previousTEThreshold
    return ((offlineDelivered - previousTEThreshold) / totalNeeded).toFloat()
}

fun getNextTruthEggThreshold(delivered: Double): Double {
    val basePassed = countTruthEggThresholdsPassed(delivered)

    return VIRTUE_DELIVERY_GOALS.getOrElse(basePassed) { 0.0 }
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

private fun formatDailyEvents(
    periodicalsData: PeriodicalsData?
): List<Event> {
    return periodicalsData?.dailyEvents?.map { event ->
        Event(
            identifier = event.identifier,
            type = event.type,
            multiplier = event.multiplier,
            isUltra = event.ccOnly,
            name = getEventName(event.type)
        )
    } ?: emptyList()
}

private fun getEventName(eventType: String): String {
    return ALL_EVENT_TYPES.find { it.id == eventType }?.displayName ?: ""
}

private fun formatVirtueFarms(
    virtue: Backup.Virtue,
    inactiveLayRate: Double
): List<VirtueFarmInfo> {
    return VIRTUE_EGGS.mapIndexed { index, egg ->
        val eggsDelivered = virtue.eggsDeliveredList.elementAtOrNull(index) ?: 0.0
        val truthEggs = virtue.eovEarnedList.elementAtOrNull(index) ?: 0
        val pendingTruthEggs = countTruthEggThresholdsPassed(eggsDelivered) - truthEggs

        VirtueFarmInfo(
            eggId = egg,
            truthEggs = truthEggs,
            pendingTruthEggs = max(0, pendingTruthEggs),
            eggsDelivered = eggsDelivered,
            inactiveLayRate = inactiveLayRate
        )
    }
}

private fun countTruthEggThresholdsPassed(delivered: Double): Int {
    var count = 0
    val maxTe = VIRTUE_DELIVERY_GOALS.size
    while (count < maxTe && delivered >= VIRTUE_DELIVERY_GOALS[count]) {
        count++
    }
    return count
}

private fun getPreviousTruthEggThreshold(delivered: Double): Double {
    val basePassed = countTruthEggThresholdsPassed(delivered)

    return VIRTUE_DELIVERY_GOALS.getOrElse(basePassed - 1) { 0.0 }
}

private fun getVirtueOfflineEggsDelivered(
    virtueInfo: VirtueInfo,
    currentEggsDelivered: Double
): Double {
    val timeElapsedSeconds = Instant.now().epochSecond.toDouble() - virtueInfo.lastBackupDate
    // Progress stops as soon as silos are empty
    min(timeElapsedSeconds, virtueInfo.maximumOfflineTime)
    val ihrPerSecond = virtueInfo.offlineHatcheryRate / 60.0
    val lastRefreshedPopulation = virtueInfo.population

    // Calculate population at which shipping capacity is maxed out
    val maxEffectivePopulation =
        (virtueInfo.shippingCapacity / virtueInfo.eggLayingRate) * lastRefreshedPopulation

    // Effective capacity is the minimum of hab capacity and shipping-limited population
    val effectiveCapacity = min(virtueInfo.habCapacity, maxEffectivePopulation)

    // If we're already at or above effective capacity, ELR is static
    if (lastRefreshedPopulation >= effectiveCapacity) {
        val staticELR = min(virtueInfo.eggLayingRate, virtueInfo.shippingCapacity)
        val eggsDeliveredWhileOffline = staticELR * timeElapsedSeconds
        return currentEggsDelivered + eggsDeliveredWhileOffline
    }

    // If we reach effective capacity during this period
    val currentPopulation = min(
        (lastRefreshedPopulation + (virtueInfo.offlineHatcheryRate / 60.0) * timeElapsedSeconds),
        max(lastRefreshedPopulation, virtueInfo.habCapacity)
    )
    if (currentPopulation >= effectiveCapacity) {
        // Calculate time to reach effective capacity
        val timeToCapacity = (effectiveCapacity - lastRefreshedPopulation) / ihrPerSecond

        // Phase 1: Growing population until effective capacity is reached (use growth formula)
        val initialELR = virtueInfo.eggLayingRate
        val linearTerm1 = initialELR * timeToCapacity
        val quadraticTerm1 =
            (initialELR * ihrPerSecond * timeToCapacity * timeToCapacity) / (2 * lastRefreshedPopulation)
        val eggsPhase1 = linearTerm1 + quadraticTerm1

        // Phase 2: Static ELR after effective capacity is reached
        val timeAfterCapacity = timeElapsedSeconds - timeToCapacity
        val staticELR = min(
            virtueInfo.eggLayingRate * (effectiveCapacity / lastRefreshedPopulation),
            virtueInfo.shippingCapacity
        )
        val eggsPhase2 = staticELR * timeAfterCapacity

        return currentEggsDelivered + eggsPhase1 + eggsPhase2
    }

    // Population stays below effective capacity - use standard growth formula
    val linearTerm = virtueInfo.eggLayingRate * timeElapsedSeconds
    val quadraticTerm =
        (virtueInfo.eggLayingRate * ihrPerSecond * timeElapsedSeconds * timeElapsedSeconds) / (2 * lastRefreshedPopulation)
    val eggsDeliveredWhileOffline = linearTerm + quadraticTerm

    return currentEggsDelivered + eggsDeliveredWhileOffline
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
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Pair<Double, Double> {
    val (onlineMultiplier, offlineMultiplier, researchBaseRate) = getResearchIhrMultiplier(
        backup,
        homeFarm
    )
    if (onlineMultiplier == 0.0) return Pair(0.0, 0.0)

    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(playerColleggtibleInfo, GameDimension.INTERNAL_HATCHERY_RATE)
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
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Pair<Double, Double> {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(playerColleggtibleInfo, GameDimension.EGG_LAYING_RATE)
    val artifactsMultiplier = getArtifactsMultiplier(
        virtueArtifacts, LAY_RATE_ARTIFACTS,
        LAY_RATE_STONES
    )
    val researchLayRate = getResearchLayRate(backup, homeFarm)

    val multipliers = researchLayRate * artifactsMultiplier * colleggtibleMultiplier

    // the lay rate of the active farm with population, and the lay rate of in active farms with 1 chicken
    return Pair(homeFarm.numChickens * multipliers, multipliers)
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
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Double {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(playerColleggtibleInfo, GameDimension.SHIPPING_CAPACITY)
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
    playerColleggtibleInfo: List<PlayerColleggtibleInfoEntry>,
    homeFarm: Backup.Simulation,
    virtueArtifacts: List<Artifact>
): Double {
    val colleggtibleMultiplier =
        getColleggtiblesMultiplier(playerColleggtibleInfo, GameDimension.HAB_CAPACITY)
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
        if (artifactSearchList.isNotEmpty()) {
            val matchingArtifact = artifactSearchList.find {
                it.name == artifact.name && it.level == artifact.level && it.rarity == artifact.rarity
            }
            if (matchingArtifact != null) {
                baseRate *= (1.0 + matchingArtifact.effectValue!!)
            }
        }

        if (stoneSearchList.isNotEmpty()) {
            artifact.stones.forEach { stone ->
                val matchingStone = stoneSearchList.find {
                    it.name == stone.name && it.level == stone.level
                }
                if (matchingStone != null) {
                    baseRate *= (1.0 + matchingStone.effectValue!!)
                }
            }
        }
    }

    return baseRate
}

private fun formatTimeText(
    timeRemainingSeconds: Double,
    useAbsoluteTime: Boolean,
    use24HrFormat: Boolean
): String {
    if (timeRemainingSeconds.isInfinite()) {
        return "Infinity"
    }

    if (timeRemainingSeconds <= 0.0) {
        return "TE Ready"
    }

    val years = timeRemainingSeconds / 31536000
    if (years >= 10) {
        return ">10y"
    }

    val days = timeRemainingSeconds / 86400

    if (useAbsoluteTime) {
        val currentTime = LocalDateTime.now()
        val endingTime = currentTime.plusSeconds(timeRemainingSeconds.toLong())
        return if (years > 1) {
            endingTime.format(DateTimeFormatter.ofPattern("yyyy MMM d"))
        } else if (days > 1) {
            if (use24HrFormat) {
                endingTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
            } else {
                endingTime.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
            }
        } else {
            if (use24HrFormat) {
                endingTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                endingTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            }
        }

    }

    val remainingSecondsAfterDays = timeRemainingSeconds % 86400

    val hours = remainingSecondsAfterDays / 3600
    val remainingSecondsAfterHours = remainingSecondsAfterDays % 3600

    val minutes = remainingSecondsAfterHours / 60

    return if (years > 1) {
        if (days.toInt() % 365 == 0) {
            "${years.toInt()}y"
        } else {
            "${years.toInt()}y ${days.toInt() % 365}d"
        }
    } else if (days > 1) {
        if (hours.toInt() == 0) {
            "${days.toInt()}d"
        } else {
            "${days.toInt()}d ${hours.toInt()}h"
        }
    } else {
        if (hours.toInt() == 0) {
            "${minutes.toInt()}m"
        } else {
            "${hours.toInt()}h ${minutes.toInt()}m"
        }
    }
}