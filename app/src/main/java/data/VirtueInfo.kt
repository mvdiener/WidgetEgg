package data

import kotlinx.serialization.Serializable

@Serializable
data class VirtueInfo(
    var stateId: String = "",
    var resets: Int = 0,
    var shifts: Int = 0,
    var soulEggs: String = "",
    var totalTruthEggs: String = "",
    var eggId: Int = 0,
    var population: String = "",
    var lastBackupDate: Double = 0.0,
    var siloCount: Int = 1,
    var maximumOfflineTime: Double = 0.0,
    var virtueEquippedArtifacts: List<Artifact> = emptyList(),
    var homeEquippedArtifacts: List<Artifact> = emptyList(),
    var dailyEvents: List<Event> = emptyList(),
    var farms: List<VirtueFarmInfo> = emptyList()
)

@Serializable
data class VirtueFarmInfo(
    val eggId: Int = 0,
    val truthEggs: Int = 0,
    val pendingTruthEggs: Int = 0,
    val eggsDelivered: Double = 0.0
)

@Serializable
data class Artifact(
    var name: Int,
    var rarity: Int,
    var level: Int,
    var stones: List<Stone>
)

@Serializable
data class Stone(
    var name: Int,
    var level: Int
)

@Serializable
data class Event(
    var type: String,
    var multiplier: Double,
    var isUltra: Boolean
)