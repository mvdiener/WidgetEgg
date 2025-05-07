package data

import ei.Ei.RewardType
import kotlinx.serialization.Serializable

// Data class used to save contract information to preferences
@Serializable
data class ContractInfoEntry(
    var eggId: Int,
    var customEggId: String?,
    var name: String,
    var seasonName: String?,
    var isLegacy: Boolean,
    var eggsDelivered: Double,
    var timeRemainingSeconds: Double,
    var allGoalsAchieved: Boolean,
    var clearedForExit: Boolean,
    var goals: List<GoalInfoEntry>,
    var contributors: List<ContributorInfoEntry>
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
    var offlineTimeSeconds: Double
)