package tools.utilities

import android.graphics.Bitmap
import androidx.core.graphics.toColorInt
import data.ContractData
import data.ContractInfoEntry
import data.ContributorInfoEntry
import data.GoalInfoEntry
import kotlin.math.abs

fun getContractGoalPercentComplete(
    delivered: Double,
    goal: Double
): Float {
    return if (delivered > goal) {
        100F
    } else {
        (delivered / goal).toFloat()
    }
}

fun formatContractData(contractInfo: ContractData): List<ContractInfoEntry> {
    var formattedContracts: List<ContractInfoEntry> = emptyList()

    contractInfo.contracts.forEach { contract ->
        var formattedGoals: List<GoalInfoEntry> = emptyList()
        val gradeSpecsList = contract.contract.gradeSpecsList
        val gradeSpecs = gradeSpecsList.find { gradeSpec -> gradeSpec.grade == contract.grade }

        gradeSpecs?.goalsList?.forEach { goal ->
            formattedGoals = formattedGoals.plus(GoalInfoEntry(amount = goal.targetAmount))
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

fun createContractCircularProgressBarBitmap(
    progress: Float,
    size: Int
): Bitmap {
    val color = "#16ac00".toColorInt()
    return createCircularProgressBarBitmap(progress, color, size, 4f)
}

// Estimated duration remaining based on offline contributions
// Returns Pair<timeText, isOnTrack>
fun getContractDurationRemaining(contract: ContractInfoEntry): Pair<String, Boolean> {
    var isOnTrack = true
    if (contract.allGoalsAchieved) {
        return Pair("Finished!", isOnTrack)
    }

    val totalEggsNeeded = contract.goals.maxOf { goal -> goal.amount }
    val totalEggsDelivered = contract.eggsDelivered
    val offlineEggsDelivered = contract.contributors.sumOf { contributor ->
        contributor.offlineTimeSeconds * contributor.eggRatePerSecond
    }

    val remainingEggsNeeded = totalEggsNeeded - totalEggsDelivered - offlineEggsDelivered
    val totalEggRatePerSecond =
        contract.contributors.sumOf { contributor -> contributor.eggRatePerSecond }

    val timeRemainingSeconds = remainingEggsNeeded / totalEggRatePerSecond
    if (timeRemainingSeconds > contract.timeRemainingSeconds) {
        isOnTrack = false
    }

    val timeText = formatTimeText(timeRemainingSeconds)

    return Pair(timeText, isOnTrack)
}

private fun formatTimeText(timeRemainingSeconds: Double): String {
    val years = (timeRemainingSeconds / 31536000).toInt()
    val remainingSecondsAfterYears = timeRemainingSeconds % 31536000

    val days = (remainingSecondsAfterYears / 86400).toInt()
    val remainingSecondsAfterDays = remainingSecondsAfterYears % 86400

    val hours = (remainingSecondsAfterDays / 3600).toInt()
    val remainingSecondsAfterHours = remainingSecondsAfterDays % 3600

    val minutes = (remainingSecondsAfterHours / 60).toInt()

    val timeText = mutableListOf<String>()

    if (years > 0) timeText += "${years}y"
    if (days > 0) timeText += "${days}d"
    if (hours > 0) timeText += "${hours}h"
    if (minutes > 0) timeText += "${minutes}m"

    return if (timeText.isEmpty()) "Finished!" else timeText.joinToString("")
}