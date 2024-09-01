package data

import ei.Ei.Backup
import ei.Ei.MissionInfo

data class MissionData(
    val missions: List<MissionInfo>,
    val artifacts: Backup.Artifacts
)
