package tools.utilities

import androidx.compose.ui.graphics.Color
import data.ALL_GRADES
import data.ALL_ROLES
import data.CRAFTING_LEVELS
import data.StatsInfo
import ei.Ei
import java.util.UUID
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
        craftingXP = numberToString(backup.artifacts.craftingXp)
    )
}

fun getFarmerRole(roleInt: Int): Pair<String, Color> {
    val allRolesSize = ALL_ROLES.size
    if (roleInt >= allRolesSize) {
        return ALL_ROLES[allRolesSize - 1]
    }

    return ALL_ROLES[roleInt]
}

fun getShortenedFarmerName(name: String): String {
    val split = name.split(" ")
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