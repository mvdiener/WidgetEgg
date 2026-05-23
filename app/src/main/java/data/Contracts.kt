package data

import ei.Ei.Contract.PlayerGrade
import ei.Ei.ContractCoopStatusResponse
import ei.Ei.LocalContract
import ei.Ei.RewardType
import kotlinx.serialization.Serializable

// Data class used as return object from api.fetchContractData
data class ContractData(
    val contracts: List<LocalContract>,
    val contractStatuses: List<ContractCoopStatusResponse>
)

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
    var isUltra: Boolean,
    var goals: List<GoalInfoEntry>,
    var contributors: List<ContributorInfoEntry>,
    var contractArtifacts: List<Artifact>
)

@Serializable
data class GoalInfoEntry(
    var amount: Double,
    var reward: RewardType,
    var rewardSubType: String,
    var rewardAmount: Double
)

@Serializable
data class ContributorInfoEntry(
    var eggsDelivered: Double,
    var eggRatePerSecond: Double,
    var offlineTimeSeconds: Double,
    var offlineTimeSecondsIgnoringSilos: Double,
    var isSelf: Boolean
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
    var grade: Int,
    var maxCoopSize: Int,
    var coopLengthSeconds: Double,
    var tokenTimerMinutes: Double,
    var isUltra: Boolean,
    var notificationSent: Boolean,
    var goals: List<GoalInfoEntry>,
    var archivedContractInfo: ArchivedContractInfoEntry?
)

@Serializable
data class ArchivedContractInfoEntry(
    var numOfGoalsAchieved: Int,
    var pointsReplay: Boolean,
    var lastScore: Double
)

@Serializable
data class SeasonGradeAndGoals(
    var stateId: String = "",
    var seasonName: String = "",
    var seasonScore: Double = 0.0,
    var startingSeasonGrade: PlayerGrade? = null,
    var goals: List<GoalInfoEntry> = emptyList()
)