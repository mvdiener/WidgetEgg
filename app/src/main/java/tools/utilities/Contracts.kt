package tools.utilities

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import data.ContractData
import data.ContractInfoEntry
import data.ContributorInfoEntry
import data.GoalInfoEntry
import ei.Ei
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

fun formatContractData(contractInfo: ContractData): List<ContractInfoEntry> {
    var formattedContracts: List<ContractInfoEntry> = emptyList()

    contractInfo.contracts.forEach { contract ->
        var formattedGoals: List<GoalInfoEntry> = emptyList()
        val gradeSpecsList = contract.contract.gradeSpecsList
        val gradeSpecs = gradeSpecsList.find { gradeSpec -> gradeSpec.grade == contract.grade }

        gradeSpecs?.goalsList?.forEach { goal ->
            formattedGoals = formattedGoals.plus(
                GoalInfoEntry(
                    goalAmount = goal.targetAmount,
                    reward = goal.rewardType,
                    rewardSubType = goal.rewardSubType
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
            contributor.userName == "[departed]" && contributor.contributionAmount == 0.0 && contributor.contributionRate == 0.0 && contributor.uuid.isNullOrEmpty()
        }?.forEach { contributor ->
            formattedContributors = formattedContributors.plus(
                ContributorInfoEntry(
                    eggsDelivered = contributor.contributionAmount,
                    eggRatePerSecond = contributor.contributionRate,
                    offlineTimeSeconds = abs(contributor.farmInfo?.timestamp ?: 0.0)
                )
            )
        }

        formattedContracts = formattedContracts.plus(
            ContractInfoEntry(
                eggId = contract.contract.egg.number,
                customEggId = contract.contract.customEggId,
                name = contract.contract.name,
                seasonName = formatSeasonName(contract.contract.seasonId),
                isLegacy = contract.contract.leggacy,
                eggsDelivered = status?.totalAmount ?: 0.0,
                timeRemainingSeconds = status?.secondsRemaining ?: 0.0,
                allGoalsAchieved = status?.allGoalsAchieved ?: false,
                clearedForExit = status?.clearedForExit ?: false,
                goals = formattedGoals,
                contributors = formattedContributors
            )
        )
    }

    return formattedContracts
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

fun createContractCircularProgressBarBitmap(
    progress: Float,
    size: Int
): Bitmap {
    val color = "#16ac00".toColorInt()
    return createCircularProgressBarBitmap(progress, color, size, 4f)
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

    val totalEggsNeeded = contract.goals.maxOf { goal -> goal.goalAmount }
    val totalEggsDelivered = contract.eggsDelivered
    var offlineEggsDelivered = 0.0

    if (useOfflineTime) {
        offlineEggsDelivered = contract.contributors.sumOf { contributor ->
            contributor.offlineTimeSeconds * contributor.eggRatePerSecond
        }
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

fun getContractTimeTextColor(contract: ContractInfoEntry, isOnTrack: Boolean): Int {
    return if (contract.timeRemainingSeconds <= 0.0 && !isOnTrack) {
        Color.Red.toArgb()
    } else if (!isOnTrack) {
        android.graphics.Color.argb(255, 255, 165, 0) //orange
    } else {
        Color.White.toArgb()
    }
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
    return when (goal.rewardSubType) {
        "epic_internal_incubators" -> "research/epic_internal_hatchery.png"
        "cheaper_research" -> "research/lab_upgrade.png"
        "int_hatch_sharing" -> "research/internal_hatchery_sharing.png"
        "int_hatch_calm" -> "research/internal_hatchery_calm.png"
        "soul_eggs" -> "research/soul_food.png"
        "afx_mission_time" -> "research/afx_mission_duration.png"
        else -> "eggs/egg_unknown.png"
    }
}

private fun getBoostImagePath(goal: GoalInfoEntry): String {
    val id = goal.rewardSubType.removeSuffix("_v2")
    return "boosts/b_icon_$id.png"
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