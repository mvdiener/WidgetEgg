package tools

import data.SHIP_TIMES
import ei.Ei.Backup
import ei.Ei.MissionInfo

fun getMissionDuration(mission: MissionInfo, epicResearch: List<Backup.ResearchItem>): Double {
    val ftlLevel = epicResearch.find { x -> x.id == "afx_mission_time" }?.level

    ftlLevel?.let {
        var seconds = SHIP_TIMES[mission.ship.number][mission.durationType.number].toDouble()
        if (mission.ship.number >= MissionInfo.Spaceship.MILLENIUM_CHICKEN_VALUE) {
            seconds *= (1 - 0.01 * it.toDouble())
        }
        return seconds
    }

    return 0.toDouble()
}