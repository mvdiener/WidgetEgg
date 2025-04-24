package tools.utilities

import android.graphics.Bitmap
import androidx.core.graphics.toColorInt
import data.ContractData
import data.ContractInfoEntry
import data.ContributorInfoEntry
import data.GoalInfoEntry

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
                    eggRate = contributor.contributionRate,
                    offlineTime = contributor.farmInfo?.timestamp ?: 0.0
                )
            )
        }

        formattedContracts = formattedContracts.plus(
            ContractInfoEntry(
                eggId = contract.contract.egg.number,
                customEggId = contract.contract.customEggId,
                name = contract.contract.name,
                eggsDelivered = status?.totalAmount ?: 0.0,
                timeRemaining = status?.secondsRemaining ?: 0.0,
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