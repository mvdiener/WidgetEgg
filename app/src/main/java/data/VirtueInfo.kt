package data

import kotlinx.serialization.Serializable

@Serializable
data class VirtueInfo(
    var stateId: String = "",
    var resets: Int = 0,
    var shifts: Int = 0,
    var nextShiftCost: String = "",
    var totalTruthEggs: String = "",
    var totalPendingTruthEggs: String = "",
    var onlineHatcheryRate: Double = 0.0,
    var offlineHatcheryRate: Double = 0.0,
    var eggId: Int = 0,
    var population: String = "",
    var lastBackupDate: Double = 0.0,
    var maximumOfflineTime: Double = 0.0,
    var isOnVirtue: Boolean = false,
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
data class Event(
    var type: String,
    var multiplier: Double,
    var isUltra: Boolean
)