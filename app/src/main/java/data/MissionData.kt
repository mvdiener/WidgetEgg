package data

import ei.Ei.Backup
import ei.Ei.MissionInfo

// Data class used as return object from api.fetchData
data class MissionData(
    val missions: List<MissionInfo>,
    val artifacts: Backup.Artifacts
)
