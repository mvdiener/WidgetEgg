package data

import kotlinx.serialization.Serializable

@Serializable
data class Artifact(
    var name: Int,
    var rarity: Int,
    var level: Int,
    var effectValue: Double? = null,
    var stones: List<Stone>
)

@Serializable
data class Stone(
    var name: Int,
    var level: Int,
    var effectValue: Double? = null,
)

data class Research(
    val id: String,
    val perLevelValue: Double,
    val isEpic: Boolean,
    val isMultiplicative: Boolean,
    val isOfflineOnly: Boolean
)