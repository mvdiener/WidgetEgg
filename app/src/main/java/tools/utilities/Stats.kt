package tools.utilities

import androidx.compose.ui.graphics.Color
import data.ALL_GRADES
import data.ALL_ROLES
import data.Badges
import data.CRAFTING_LEVELS
import data.StatsInfo
import ei.Ei
import java.util.UUID
import kotlin.Boolean
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun formatStatsData(backup: Ei.Backup): StatsInfo {
    val eb = calculateEB(backup)
    val roleId = getEBRoleId(eb)
    val geBalance = (backup.game.goldenEggsEarned - backup.game.goldenEggsSpent).toDouble()
    val ticketBalance = (backup.game.shellScriptsEarned - backup.game.shellScriptsSpent).toDouble()

    val homeFarm = backup.farmsList.first { it.farmType == Ei.FarmType.HOME }
    val contractInfo = backup.contracts.lastCpi

    val totalMissions =
        backup.artifactsDb.missionArchiveCount + backup.artifactsDb.missionInfosCount

    return StatsInfo(
        stateId = UUID.randomUUID().toString(),
        hasProPermit = backup.game.permitLevel > 0,
        soulEggs = numberToString(backup.game.soulEggsD),
        prophecyEggs = backup.game.eggsOfProphecy.toString(),
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
        badges = getBadges(backup)
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

fun getBadges(backup: Ei.Backup): Badges {
    val allLegendaries = getAllLegendaries(backup.artifactsDb.inventoryItemsList)

    // Some calculations may need to change, if new elements are added to the game
    return Badges(
        hasAlc = getDistinctLegendaries(allLegendaries) >= 22,
        hasAsc = totalShipStars(backup) >= 49,
        hasCraftingLegend = getCraftingLevel(backup.artifacts.craftingXp) == 30,
        hasEnd = hasEnded(backup),
        hasNah = hasNah(backup),
        hasFed = hasFed(backup),
        hasAllShells = false, // this requires an entire other endpoint call to figure out, so leaving this out for now
        hasZlc = allLegendaries.isEmpty()
    )
}

private fun getAllLegendaries(inventory: List<Ei.ArtifactInventoryItem>): List<Ei.ArtifactInventoryItem> {
    return inventory.filter { artifact ->
        artifact.artifact.spec.rarity == Ei.ArtifactSpec.Rarity.LEGENDARY
    }
}

private fun getDistinctLegendaries(inventory: List<Ei.ArtifactInventoryItem>): Int {
    return inventory.distinctBy { artifact ->
        Pair(
            artifact.artifact.spec.name,
            artifact.artifact.spec.level
        )
    }.size
}

private fun totalShipStars(backup: Ei.Backup): Int {
    val allMissions = backup.artifactsDb.missionInfosList + backup.artifactsDb.missionArchiveList
    val missionsSorted = allMissions.sortedByDescending { ship -> ship.startTimeDerived }

    val distinctMissions = missionsSorted.distinctBy { mission -> mission.ship }
    return distinctMissions.sumOf { mission -> mission.level }
}

private fun hasEnded(backup: Ei.Backup): Boolean {
    if (backup.game.eggMedalLevelList.size < Ei.Egg.ENLIGHTENMENT.number) return false

    return backup.game.eggMedalLevelList[Ei.Egg.ENLIGHTENMENT.number - 1] >= 5
}

private fun hasNah(backup: Ei.Backup): Boolean {
    if (backup.game.maxFarmSizeReachedList.size < Ei.Egg.ENLIGHTENMENT.number) return false

    return backup.game.maxFarmSizeReachedList[Ei.Egg.ENLIGHTENMENT.number - 1] >= 19845000000L
}

private fun hasFed(backup: Ei.Backup): Boolean {
    if (backup.game.maxFarmSizeReachedList.size < Ei.Egg.ENLIGHTENMENT.number) return false
    val enlightenEgg = Ei.Egg.ENLIGHTENMENT.number - 1
    return backup.game.maxFarmSizeReachedList.withIndex().firstOrNull { (i, farmSize) ->
        if (i == enlightenEgg) {
            farmSize < 19845000000L
        } else {
            farmSize < 14175000000L
        }
    } == null
}

private fun calculateEB(backup: Ei.Backup): Double {
    val peCount = backup.game.eggsOfProphecy
    val seCount = backup.game.soulEggsD

    val peResearch = backup.game.epicResearchList.first { it.id == "prophecy_bonus" }.level
    val seResearch = backup.game.epicResearchList.first { it.id == "soul_eggs" }.level

    val bonusFactor = peResearch.toDouble() * 0.01
    val perEgg = (10.0 + seResearch.toDouble()) * ((1.05 + bonusFactor).pow(peCount.toDouble()))

    return seCount * perEgg
}

private fun getEBRoleId(eb: Double): Int {
    return min((log10(max(eb, 1.0))).toInt(), ALL_ROLES.size - 1)
}

private fun getCraftingLevel(craftingXp: Double): Int {
    var xpRequired = 0.0
    CRAFTING_LEVELS.forEachIndexed { i, level ->
        xpRequired = xpRequired + CRAFTING_LEVELS[i]
        if (craftingXp < xpRequired) {
            return i + 1
        }
    }

    return 30
}