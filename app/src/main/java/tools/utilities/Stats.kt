package tools.utilities

import androidx.compose.ui.graphics.Color
import data.ALL_GRADES
import data.ALL_ROLES
import data.Badges
import data.CRAFTING_LEVELS
import data.CustomEggInfoEntry
import data.MAX_ENLIGHTEN_FARM_POP
import data.MAX_FARM_POP
import data.PeriodicalsData
import data.SHIP_MAX_LAUNCH_POINTS
import data.StatsInfo
import ei.Ei.ArtifactInventoryItem
import ei.Ei.ArtifactSpec
import ei.Ei.Backup
import ei.Ei.Egg
import ei.Ei.FarmType
import ei.Ei.GameModifier
import ei.Ei.MissionInfo
import java.util.UUID
import kotlin.Boolean
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

fun formatStatsData(backup: Backup, customEggs: List<CustomEggInfoEntry>): StatsInfo {
    val eb = calculateEB(backup)
    val roleId = getEBRoleId(eb)
    val geBalance = (backup.game.goldenEggsEarned - backup.game.goldenEggsSpent).toDouble()
    val ticketBalance = (backup.game.shellScriptsEarned - backup.game.shellScriptsSpent).toDouble()

    val homeFarm = backup.farmsList.first { it.farmType == FarmType.HOME }
    val contractInfo = backup.contracts.lastCpi

    val totalMissions =
        backup.artifactsDb.missionArchiveCount + backup.artifactsDb.missionInfosCount

    return StatsInfo(
        stateId = UUID.randomUUID().toString(),
        hasProPermit = backup.game.permitLevel > 0,
        soulEggs = numberToString(backup.game.soulEggsD),
        prophecyEggs = backup.game.eggsOfProphecy.toString(),
        truthEggs = backup.virtue.eovEarnedList.sumOf { it }.toString(),
        farmerRoleId = roleId,
        earningsBonus = numberToString(eb),
        goldEggs = numberToString(geBalance),
        tickets = numberToString(ticketBalance),
        homeFarmEggId = homeFarm.eggType.number,
        homeFarmPopulation = numberToString(homeFarm.numChickens.toDouble()),
        contractGrade = contractInfo.grade.number,
        contractSeasonScore = numberToString(contractInfo.seasonCxp),
        contractTotalScore = numberToString(contractInfo.totalCxp),
        shipsLaunched = numberToString(totalMissions.toDouble()),
        droneTakedowns = numberToString(backup.stats.droneTakedowns.toDouble()),
        craftingLevel = getCraftingLevel(backup.artifacts.craftingXp),
        craftingXP = numberToString(backup.artifacts.craftingXp),
        badges = getBadges(backup, customEggs)
    )
}

fun getFarmerRole(roleInt: Int): Pair<String, Color> {
    val allRolesSize = ALL_ROLES.size
    if (roleInt >= allRolesSize) {
        return ALL_ROLES[allRolesSize - 1]
    }

    return ALL_ROLES[roleInt]
}

fun getShortenedFarmerRole(role: String): String {
    val split = role.split(" ")
    return if (split.size == 1) {
        "Hmm..."
    } else {
        "${split[0].first()}${split[1]}"
    }
}

fun getContractGradeName(grade: Int): String {
    val allGradesSize = ALL_GRADES.size
    if (grade >= allGradesSize) {
        return ALL_GRADES[0]
    }

    return ALL_GRADES[grade]
}

fun getBadges(backup: Backup, customEggs: List<CustomEggInfoEntry>): Badges {
    val allLegendaries = getAllLegendaries(backup.artifactsDb.inventoryItemsList)
    val habsMultiplier = getHabsColleggtiblesMultiplier(customEggs)

    val (hasFed, hasFedPlus) = hasFed(backup, habsMultiplier)

    // Some calculations may need to change, if new elements are added to the game
    return Badges(
        hasAlc = getDistinctLegendaries(allLegendaries) >= 22,
        hasAsc = hasAsc(backup),
        hasCraftingLegend = getCraftingLevel(backup.artifacts.craftingXp) == 30,
        hasEnd = hasEnded(backup),
        hasNah = hasNah(backup),
        hasNahPlus = if (habsMultiplier == 1.0) false else hasNah(backup, habsMultiplier),
        hasFed = hasFed,
        hasFedPlus = if (habsMultiplier == 1.0) false else hasFedPlus,
        hasAllShells = false, // this requires an entire other endpoint call to figure out, so leaving this out for now
        hasZlc = allLegendaries.isEmpty()
    )
}

private fun getAllLegendaries(inventory: List<ArtifactInventoryItem>): List<ArtifactInventoryItem> {
    return inventory.filter { artifact ->
        artifact.artifact.spec.rarity == ArtifactSpec.Rarity.LEGENDARY
    }
}

private fun getDistinctLegendaries(inventory: List<ArtifactInventoryItem>): Int {
    return inventory.distinctBy { artifact ->
        Pair(
            artifact.artifact.spec.name,
            artifact.artifact.spec.level
        )
    }.size
}

private fun hasAsc(backup: Backup): Boolean {
    val allMissions = backup.artifactsDb.missionInfosList + backup.artifactsDb.missionArchiveList
    val missionsByShip = allMissions.groupBy { it.ship }

    if (missionsByShip.size != SHIP_MAX_LAUNCH_POINTS.size) return false

    val launchPointsByShip = missionsByShip.mapValues { (_, missions) ->
        calculateLaunchPoints(missions)
    }

    launchPointsByShip.forEach { (ship, launchPoints) ->
        if (launchPoints < SHIP_MAX_LAUNCH_POINTS[ship.number]) return false
    }

    return true
}

private fun calculateLaunchPoints(missions: List<MissionInfo>): Double {
    val calculatedPoints = missions.sumOf { mission ->
        when (mission.durationType) {
            MissionInfo.DurationType.SHORT, MissionInfo.DurationType.TUTORIAL -> 1.0
            MissionInfo.DurationType.LONG -> 1.4
            MissionInfo.DurationType.EPIC -> 1.8
            else -> 0.0
        }
    }

    return round(calculatedPoints * 10) / 10.0
}

private fun hasEnded(backup: Backup): Boolean {
    if (backup.game.eggMedalLevelList.size < Egg.ENLIGHTENMENT.number) return false

    return backup.game.eggMedalLevelList[Egg.ENLIGHTENMENT.number - 1] >= 5
}

private fun hasNah(backup: Backup, multiplier: Double = 1.0): Boolean {
    val enlightenmentIndex = Egg.ENLIGHTENMENT.number - 1
    if (backup.game.maxFarmSizeReachedList.size <= enlightenmentIndex) return false

    val targetPop = (MAX_ENLIGHTEN_FARM_POP * multiplier).toLong()
    return backup.game.maxFarmSizeReachedList[enlightenmentIndex] >= targetPop
}

private fun hasFed(backup: Backup, multiplier: Double = 1.0): Pair<Boolean, Boolean> {
    val enlightenmentIndex = Egg.ENLIGHTENMENT.number - 1
    if (backup.game.maxFarmSizeReachedList.size <= enlightenmentIndex) return Pair(false, false)

    var isFed = true
    var isFedPlus = true

    backup.game.maxFarmSizeReachedList.forEachIndexed { i, farmSize ->
        val population = if (i == enlightenmentIndex) MAX_ENLIGHTEN_FARM_POP else MAX_FARM_POP
        val populationPlus = (population * multiplier).toLong()

        if (farmSize < population) isFed = false
        if (farmSize < populationPlus) isFedPlus = false

        if (!isFed && !isFedPlus) return Pair(false, false)
    }

    return Pair(isFed, isFedPlus)
}

private fun calculateEB(backup: Backup): Double {
    val peCount = backup.game.eggsOfProphecy
    val seCount = backup.game.soulEggsD
    val teCount = backup.virtue.eovEarnedList.sumOf { it }.toDouble()

    val peResearch = backup.game.epicResearchList.first { it.id == "prophecy_bonus" }.level
    val seResearch = backup.game.epicResearchList.first { it.id == "soul_eggs" }.level

    val bonusFactor = peResearch.toDouble() * 0.01
    val perEgg =
        (10.0 + seResearch.toDouble()) * ((1.05 + bonusFactor).pow(peCount.toDouble())) * (1.01.pow(
            teCount
        ))

    return seCount * perEgg
}

private fun getEBRoleId(eb: Double): Int {
    return min((log10(max(eb, 1.0))).toInt(), ALL_ROLES.size - 1)
}

private fun getCraftingLevel(craftingXp: Double): Int {
    var xpRequired = 0.0
    CRAFTING_LEVELS.forEachIndexed { i, _ ->
        xpRequired += CRAFTING_LEVELS[i]
        if (craftingXp < xpRequired) {
            return i + 1
        }
    }

    return 30
}

private fun getHabsColleggtiblesMultiplier(customEggs: List<CustomEggInfoEntry>): Double {
    return customEggs.filter { egg -> egg.buff.type == GameModifier.GameDimension.HAB_CAPACITY && egg.buff.maxValue > 0.0 }
        .fold(1.0) { total, egg ->
            total * egg.buff.maxValue
        }
}