package data

import ei.Ei.RewardType
import kotlinx.serialization.Serializable

// Data class used to save contract information to preferences
@Serializable
data class ContractInfoEntry(
    var stateId: String,
    var eggId: Int,
    var customEggId: String?,
    var name: String, // EI name of contract
    var identifier: String, // EI identifier of contract
    var coopName: String, // User created name of coop
    var seasonName: String?,
    var isLegacy: Boolean,
    var eggsDelivered: Double,
    var timeRemainingSeconds: Double,
    var allGoalsAchieved: Boolean,
    var clearedForExit: Boolean,
    var grade: Int,
    var maxCoopSize: Int,
    var tokenTimerMinutes: Double,
    var goals: List<GoalInfoEntry>,
    var contributors: List<ContributorInfoEntry>,
    var contractArtifacts: List<ContractArtifact>
)

@Serializable
data class GoalInfoEntry(
    var goalAmount: Double,
    var reward: RewardType,
    var rewardSubType: String
)

@Serializable
data class ContributorInfoEntry(
    var eggsDelivered: Double,
    var eggRatePerSecond: Double,
    var offlineTimeSeconds: Double,
    var isSelf: Boolean
)

@Serializable
data class ContractArtifact(
    var artifactName: Int,
    var artifactRarity: Int,
    var artifactLevel: Int,
    var stoneList: List<ContractStone>
)

@Serializable
data class ContractStone(
    var stoneName: Int,
    var stoneLevel: Int
)

// Data class used to save periodicals contract information to preferences
@Serializable
data class PeriodicalsContractInfoEntry(
    var stateId: String,
    var eggId: Int,
    var customEggId: String?,
    var name: String,
    var identifier: String,
    var seasonName: String?,
    var isLegacy: Boolean,
    var maxCoopSize: Int,
    var coopLengthSeconds: Double,
    var tokenTimerMinutes: Double,
    var goals: List<GoalInfoEntry>,
)