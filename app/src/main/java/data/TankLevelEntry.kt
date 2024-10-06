package data

import kotlinx.serialization.Serializable

// Data class used to save mission fuel tank info to preferences
@Serializable
data class TankLevelEntry(
    var eggId: Int,
    var fuelQuantity: Double
)
