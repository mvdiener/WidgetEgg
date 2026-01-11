package tools.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import data.ArchivedContractInfoEntry
import data.CONTRACT_OFFLINE_PROGRESS_COLOR
import data.CONTRACT_PROGRESS_COLOR
import data.ContractArtifact
import data.ContractData
import data.ContractInfoEntry
import data.ContractStone
import data.ContributorInfoEntry
import data.GoalInfoEntry
import data.PeriodicalsContractInfoEntry
import data.PeriodicalsData
import ei.Ei
import ei.Ei.Backup
import ei.Ei.ContractCoopStatusResponse.ContributionInfo
import ei.Ei.LocalContract
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

fun formatContractData(
    contractInfo: ContractData,
    userName: String,
    periodicalsContracts: List<PeriodicalsContractInfoEntry>,
    contractsArchive: List<LocalContract>
): List<ContractInfoEntry> {
    var formattedContracts: List<ContractInfoEntry> = emptyList()

    contractInfo.contracts.forEach { contract ->
        var formattedGoals: List<GoalInfoEntry> = emptyList()
        val gradeSpecsList = contract.contract.gradeSpecsList
        val gradeSpecs = gradeSpecsList.find { gradeSpec -> gradeSpec.grade == contract.grade }

        gradeSpecs?.goalsList?.forEach { goal ->
            formattedGoals = formattedGoals.plus(
                GoalInfoEntry(
                    amount = goal.targetAmount,
                    reward = goal.rewardType,
                    rewardSubType = goal.rewardSubType,
                    rewardAmount = goal.rewardAmount
                )
            )
        }

        val status =
            contractInfo.contractStatuses.find { contractStatus ->
                contractStatus.contractIdentifier == contract.contract.identifier
            }

        var formattedContributors: List<ContributorInfoEntry> = emptyList()
        status?.contributorsList?.filterNot { contributor ->
            // Remove any [departed] users stuck from contract creation
            contributor.userName == "[departed]" && contributor.uuid.isNullOrEmpty()
        }?.forEach { contributor ->
            formattedContributors = formattedContributors.plus(
                ContributorInfoEntry(
                    eggsDelivered = contributor.contributionAmount,
                    eggRatePerSecond = contributor.contributionRate,
                    offlineTimeSeconds = getOfflineTime(contributor),
                    isSelf = contributor.userName == userName
                )
            )
        }

        var contractArtifacts: List<ContractArtifact> = emptyList()
        status?.contributorsList?.find { contributor ->
            contributor.userName == userName
        }?.farmInfo?.equippedArtifactsList?.forEach { artifact ->
            var stones: List<ContractStone> = emptyList()
            artifact.stonesList.forEach { stone ->
                stones = stones.plus(
                    ContractStone(
                        name = stone.name.number,
                        level = stone.level.number
                    )
                )
            }

            contractArtifacts = contractArtifacts.plus(
                ContractArtifact(
                    name = artifact.spec.name.number,
                    rarity = artifact.spec.rarity.number,
                    level = artifact.spec.level.number,
                    stones = stones
                )
            )
        }

        val periodicalContract =
            periodicalsContracts.find { it.identifier == contract.contract.identifier }

        val archivedContract = getArchivedContract(contract.contract.identifier, contractsArchive)

        formattedContracts = formattedContracts.plus(
            ContractInfoEntry(
                stateId = UUID.randomUUID()
                    .toString(), // Probably not necessary, but used in the off chance server data is not different from the last api call
                eggId = contract.contract.egg.number,
                customEggId = contract.contract.customEggId,
                name = contract.contract.name,
                identifier = contract.contract.identifier,
                coopName = contract.coopIdentifier,
                seasonName = formatSeasonName(contract.contract.seasonId),
                isLegacy = contract.contract.leggacy,
                eggsDelivered = status?.totalAmount ?: 0.0,
                timeRemainingSeconds = status?.secondsRemaining ?: 0.0,
                allGoalsAchieved = status?.allGoalsAchieved == true,
                clearedForExit = status?.clearedForExit == true,
                grade = contract.grade.number,
                maxCoopSize = periodicalContract?.maxCoopSize ?: 0,
                tokenTimerMinutes = periodicalContract?.tokenTimerMinutes ?: 0.0,
                isUltra = contract.contract.ccOnly,
                goals = formattedGoals,
                contributors = formattedContributors,
                contractArtifacts = contractArtifacts,
                archivedContractInfo = archivedContract
            )
        )
    }

    return formattedContracts
}

fun formatPeriodicalsContracts(
    periodicalsData: PeriodicalsData,
    backup: Backup,
    contractsArchive: List<LocalContract>
): List<PeriodicalsContractInfoEntry> {
    var formattedContracts: List<PeriodicalsContractInfoEntry> = emptyList()
    periodicalsData.contracts.forEach { contract ->
        var formattedGoals: List<GoalInfoEntry> = emptyList()
        val gradeSpecsList = contract.gradeSpecsList
        val gradeSpecs =
            gradeSpecsList.find { gradeSpec -> gradeSpec.grade == backup.contracts.lastCpi.grade }

        gradeSpecs?.goalsList?.forEach { goal ->
            formattedGoals = formattedGoals.plus(
                GoalInfoEntry(
                    amount = goal.targetAmount,
                    reward = goal.rewardType,
                    rewardSubType = goal.rewardSubType,
                    rewardAmount = goal.rewardAmount
                )
            )
        }

        val archivedContract = getArchivedContract(contract.identifier, contractsArchive)

        formattedContracts = formattedContracts.plus(
            PeriodicalsContractInfoEntry(
                stateId = UUID.randomUUID().toString(),
                eggId = contract.egg.number,
                customEggId = contract.customEggId,
                name = contract.name,
                identifier = contract.identifier,
                seasonName = formatSeasonName(contract.seasonId),
                isLegacy = contract.leggacy,
                grade = gradeSpecs?.grade?.number ?: 0,
                maxCoopSize = contract.maxCoopSize,
                coopLengthSeconds = contract.lengthSeconds,
                tokenTimerMinutes = contract.minutesPerToken,
                isUltra = contract.ccOnly,
                goals = formattedGoals,
                archivedContractInfo = archivedContract
            )
        )
    }

    return formattedContracts
}

fun getArchivedContract(
    contractIdentifier: String,
    contractsArchive: List<LocalContract>
): ArchivedContractInfoEntry? {
    val archivedContract =
        contractsArchive.find { it.contract.identifier == contractIdentifier }

    return if (archivedContract != null) {
        ArchivedContractInfoEntry(
            numOfGoalsAchieved = archivedContract.numGoalsAchieved,
            pointsReplay = archivedContract.pointsReplay
        )
    } else {
        null
    }
}

fun getContractGoalPercentComplete(
    delivered: Double,
    goal: Double
): Float {
    return if (delivered >= goal) {
        1F
    } else {
        (delivered / goal).toFloat()
    }
}

fun getOfflineEggsDelivered(contributors: List<ContributorInfoEntry>): Double {
    return contributors.sumOf { contributor ->
        contributor.offlineTimeSeconds * contributor.eggRatePerSecond
    }
}

fun createContractCircularProgressBarBitmap(
    totalProgress: Float,
    offlineProgress: Float?,
    size: Int
): Bitmap {
    val totalProgressColor = CONTRACT_PROGRESS_COLOR.toColorInt()
    val offlineProgressColor = CONTRACT_OFFLINE_PROGRESS_COLOR.toColorInt()
    val progressData = mutableListOf(ProgressData(totalProgress, totalProgressColor))
    if (offlineProgress != null) {
        progressData.add(ProgressData(offlineProgress, offlineProgressColor))
    }
    return createCircularProgressBarBitmap(progressData, size, 4f)
}

fun createGlowBitmap(rarity: Int, sizePx: Int = 100): Bitmap {
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f
    val color = getRarityColor(rarity)
    val stops = floatArrayOf(0.0f, 0.8f, 1.0f)

    val paint = Paint().apply {
        isAntiAlias = true
        shader = RadialGradient(
            center, center, center,
            intArrayOf(color, (color and 0x00FFFFFF) or (0x66 shl 24), Color.Transparent.toArgb()),
            stops,
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(center, center, center, paint)
    return bitmap
}

// Returns Pair<timeText, isOnTrack>
fun getContractDurationRemaining(
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    use24HrFormat: Boolean,
    useOfflineTime: Boolean,
): Pair<String, Boolean> {
    var isOnTrack = true
    if (contract.allGoalsAchieved) {
        return Pair("Finished!", isOnTrack)
    }

    val totalEggsNeeded = contract.goals.maxOf { goal -> goal.amount }
    val totalEggsDelivered = contract.eggsDelivered
    var offlineEggsDelivered = 0.0

    if (useOfflineTime) {
        offlineEggsDelivered = getOfflineEggsDelivered(contract.contributors)
    }

    val remainingEggsNeeded = totalEggsNeeded - totalEggsDelivered - offlineEggsDelivered
    val totalEggRatePerSecond =
        contract.contributors.sumOf { contributor -> contributor.eggRatePerSecond }

    val timeRemainingSeconds =
        (if (remainingEggsNeeded > 0.0) remainingEggsNeeded else 0.0) / totalEggRatePerSecond
    if (timeRemainingSeconds > contract.timeRemainingSeconds) {
        isOnTrack = false

        if (contract.timeRemainingSeconds <= 0.0 && remainingEggsNeeded > 0.0) {
            return Pair("Out of time!", isOnTrack)
        }
    }

    val timeText = formatTimeText(timeRemainingSeconds, useAbsoluteTime, use24HrFormat)

    return Pair(timeText, isOnTrack)
}

fun getIndividualEggsPerHour(contributor: ContributorInfoEntry): String {
    val eggsPerHour = contributor.eggRatePerSecond * 60 * 60
    return "${numberToString(eggsPerHour)}/h"
}

fun getCoopEggsPerHour(contributors: List<ContributorInfoEntry>): String {
    val totalEggRatePerSecond = contributors.sumOf { contributor -> contributor.eggRatePerSecond }
    val totalEggRatePerHour = totalEggRatePerSecond * 60 * 60
    return "${numberToString(totalEggRatePerHour)}/h"
}

fun getOfflineTimeHoursAndMinutes(contributor: ContributorInfoEntry): String {
    val offlineTimeSeconds = contributor.offlineTimeSeconds

    if (offlineTimeSeconds <= 0) return "0m"

    val hours = (offlineTimeSeconds / 3600).toInt()
    val minutes = ((offlineTimeSeconds % 3600) / 60).toInt()

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun getContractTotalTimeText(seconds: Double): String {
    if (seconds <= 0) return "0s"

    val days = (seconds / 86400).toInt()
    val hours = ((seconds % 86400) / 3600).toInt()
    val minutes = ((seconds % 3600) / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()

    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (remainingSeconds > 0 || parts.isEmpty()) parts.add("${remainingSeconds}s")

    return parts.joinToString(" ")
}

fun getScrollName(contract: ContractInfoEntry, timeText: String): String {
    return if (contract.allGoalsAchieved && contract.clearedForExit) {
        "green_scroll"
    } else if (contract.allGoalsAchieved) {
        "yellow_scroll"
    } else if (contract.timeRemainingSeconds <= 0.0 && timeText == "Out of time!") {
        "red_scroll"
    } else {
        ""
    }
}

fun getContractTimeTextColor(
    contract: ContractInfoEntry,
    isOnTrack: Boolean,
    textColor: Color
): Int {
    return if (contract.timeRemainingSeconds <= 0.0 && !isOnTrack) {
        Color.Red.toArgb()
    } else if (!isOnTrack) {
        android.graphics.Color.argb(255, 255, 165, 0) //orange
    } else {
        textColor.toArgb()
    }
}

fun formatTokenTimeText(tokenTimerMinutes: Double): String {
    return "${tokenTimerMinutes.toInt()}m"
}

fun getRewardIconPath(goal: GoalInfoEntry): String {
    return when (goal.reward) {
        Ei.RewardType.GOLD -> "other/icon_golden_egg.png"
        Ei.RewardType.SOUL_EGGS -> "eggs/egg_soul.png"
        Ei.RewardType.EGGS_OF_PROPHECY -> "eggs/egg_of_prophecy.png"
        Ei.RewardType.EPIC_RESEARCH_ITEM -> getEpicResearchImagePath(goal)
        Ei.RewardType.PIGGY_FILL -> "other/icon_piggy_golden_egg.png"
        Ei.RewardType.PIGGY_LEVEL_BUMP -> "other/icon_piggy_level_up.png"
        Ei.RewardType.BOOST -> getBoostImagePath(goal)
        Ei.RewardType.ARTIFACT_CASE -> "other/icon_afx_chest_3.png"
        Ei.RewardType.SHELL_SCRIPT -> "other/icon_shell_script.png"
        else -> "eggs/egg_unknown.png"
    }
}

private fun getEpicResearchImagePath(goal: GoalInfoEntry): String {
    val name = when (goal.rewardSubType) {
        "epic_internal_incubators" -> "epic_internal_hatchery"
        "cheaper_research" -> "lab_upgrade"
        "int_hatch_sharing" -> "internal_hatchery_sharing"
        "int_hatch_calm" -> "internal_hatchery_calm"
        "soul_eggs" -> "soul_food"
        "afx_mission_time" -> "afx_mission_duration"
        else -> goal.rewardSubType
    }

    return "research/r_icon_${name}.png"
}

private fun getBoostImagePath(goal: GoalInfoEntry): String {
    val id = goal.rewardSubType.removeSuffix("_v2")
    return "boosts/b_icon_$id.png"
}

private fun getOfflineTime(contributor: ContributionInfo): Double {
    val currentOfflineTime = abs(contributor.farmInfo?.timestamp ?: 0.0)
    if (currentOfflineTime == 0.0) return currentOfflineTime // farm is probably private

    val baseSiloTimeSeconds = 3600.0
    val siloResearchLevel =
        contributor.farmInfo?.epicResearchList?.find { research -> research.id == "silo_capacity" }?.level
            ?: 0
    val silosBuilt = contributor.farmInfo?.silosOwned ?: 1
    val maximumOfflineTimeSeconds = (baseSiloTimeSeconds + (siloResearchLevel * 360)) * silosBuilt

    return if (currentOfflineTime > maximumOfflineTimeSeconds) {
        maximumOfflineTimeSeconds
    } else {
        currentOfflineTime
    }
}

private fun formatTimeText(
    timeRemainingSeconds: Double,
    useAbsoluteTime: Boolean,
    use24HrFormat: Boolean
): String {
    if (timeRemainingSeconds.isInfinite() || (timeRemainingSeconds / 31536000) >= 1) {
        return ">1y"
    }

    val days = timeRemainingSeconds / 86400

    if (useAbsoluteTime) {
        val currentTime = LocalDateTime.now()
        val endingTime = currentTime.plusSeconds(timeRemainingSeconds.toLong())
        return if (days > 1) {
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

    return if (days > 1) {
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

private fun formatSeasonName(seasonId: String): String {
    return seasonId.split("-", "_").joinToString(" ") { part ->
        part.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }
    }
}

private fun getRarityColor(rarity: Int): Int {
    return when (rarity) {
        //rare
        1 -> android.graphics.Color.rgb(141, 217, 255) //light blue
        //epic
        2 -> android.graphics.Color.rgb(253, 64, 253) //purple
        //legendary
        3 -> android.graphics.Color.rgb(246, 216, 63) //yellow
        //common
        else -> android.graphics.Color.rgb(255, 255, 255) //white
    }
}