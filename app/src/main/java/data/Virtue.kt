package data

import kotlinx.serialization.Serializable

// Data class used to save virtue information to preferences
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
    var eggLayingRate: Double = 0.0,
    var shippingCapacity: Double = 0.0,
    var habCapacity: Double = 0.0,
    var eggId: Int = 0,
    var population: Double = 0.0,
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
    val eggsDelivered: Double = 0.0,
    val inactiveLayRate: Double = 0.0
)

@Serializable
data class Event(
    var type: String,
    var multiplier: Double,
    var isUltra: Boolean
)

data class Research(
    val id: String,
    val perLevelValue: Double,
    val isEpic: Boolean = false,
    val isMultiplicative: Boolean = false,
    val isOfflineOnly: Boolean = false,
    val isHoverOnly: Boolean = false,
    val isHyperloopOnly: Boolean = false,
    val isPortalHabsOnly: Boolean = false
)

data class Vehicle(
    val id: Int,
    val baseCapacity: Double
)

data class Hab(
    val id: Int,
    val baseHabSpace: Double
)