package data

import kotlinx.serialization.Serializable

// Data class used to save mission information to preferences
@Serializable
data class MissionInfoEntry(
    var secondsRemaining: Double,
    var missionDuration: Double,
    var date: Long,
    var shipId: Int,
    var capacity: Int,
    var shipLevel: Int,
    var targetArtifact: Int,
    var durationType: Int,
    var identifier: String,
    var eventScheduled: Boolean
)