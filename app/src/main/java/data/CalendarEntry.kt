package data

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEntry(
    val id: Long = -1,
    val displayName: String = "Select a calendar..."
)