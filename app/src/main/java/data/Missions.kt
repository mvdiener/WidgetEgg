package data

import ei.Ei.MissionInfo
import kotlinx.serialization.Serializable

// Data class used as return object from api.fetchMissionData
data class MissionData(
    val missions: List<MissionInfo>,
    val virtueMissions: List<MissionInfo>
)

// Data class used to save mission information to preferences
@Serializable
data class MissionInfoEntry(
    var stateId: String,
    var secondsRemaining: Double,
    var missionDuration: Double,
    var date: Long,
    var shipId: Int,
    var capacity: Int,
    var shipLevel: Int,
    var targetArtifact: Int,
    var durationType: Int,
    var identifier: String
)