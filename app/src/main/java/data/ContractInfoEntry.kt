package data

import kotlinx.serialization.Serializable

// Data class used to save contract information to preferences
@Serializable
data class ContractInfoEntry(
    var eggId: Int,
    var customEggId: String?,
    var name: String,
    var eggsDelivered: Double,
    var timeRemaining: Double,
    var allGoalsAchieved: Boolean,
    var clearedForExit: Boolean,
    var goals: List<GoalInfoEntry>,
    var contributors: List<ContributorInfoEntry>
)

@Serializable
data class GoalInfoEntry(
    var amount: Double
)

@Serializable
data class ContributorInfoEntry(
    var eggsDelivered: Double,
    var eggRate: Double,
    var offlineTime: Double
)