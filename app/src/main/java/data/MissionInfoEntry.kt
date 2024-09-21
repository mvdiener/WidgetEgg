package data

import kotlinx.serialization.Serializable

@Serializable
data class MissionInfoEntry(
    var secondsRemaining: Double,
    var missionDuration: Double,
    var date: Long,
    var shipId: Int,
)