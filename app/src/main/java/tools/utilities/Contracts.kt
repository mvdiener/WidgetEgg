package tools.utilities

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.capitalize
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
        var hasProphecyEgg = false

        gradeSpecs?.goalsList?.forEach { goal ->
            formattedGoals = formattedGoals.plus(GoalInfoEntry(amount = goal.targetAmount))
            if (goal.rewardType == Ei.RewardType.EGGS_OF_PROPHECY) {
                hasProphecyEgg = true
            }
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
                hasProphecyEgg = hasProphecyEgg,
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

    val totalEggsNeeded = contract.goals.maxOf { goal -> goal.amount }
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