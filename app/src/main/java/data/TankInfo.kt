package data

import kotlinx.serialization.Serializable

// Data class used to save mission fuel tank info to preferences
@Serializable
data class TankInfo(
    var level: Int = 0,
    var fuelLevels: List<FuelLevelInfo> = emptyList()
)

@Serializable
data class FuelLevelInfo(
    var eggId: Int,
    var fuelQuantity: Double
)
