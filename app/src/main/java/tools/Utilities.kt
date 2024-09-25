package tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import data.MissionData
import data.ALL_SHIPS
import data.MissionInfoEntry
import data.SHIP_TIMES
import ei.Ei.Backup
import ei.Ei.MissionInfo
import java.time.Instant

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

fun getMissionPercentComplete(
    missionDuration: Double,
    timeRemaining: Double,
    savedTime: Long
): Float {
    var newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    if (newTimeRemaining <= 0) newTimeRemaining = 0.0
    return ((missionDuration - newTimeRemaining) / missionDuration).toFloat()
}

fun getMissionDurationRemaining(timeRemaining: Double, savedTime: Long): String {
    val newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    if (newTimeRemaining <= 0) {
        return "Finished!"
    } else {
        val days = newTimeRemaining / 86400
        val hoursMinusDays = (newTimeRemaining % 86400) / 3600
        val hours = newTimeRemaining / 3600
        val minutes = (newTimeRemaining % 3600) / 60

        return if (days > 1) {
            "${days.toInt()}d ${hoursMinusDays.toInt()}hr"
        } else {
            "${hours.toInt()}hr ${minutes.toInt()}min"
        }
    }
}

fun formatMissionData(missionInfo: MissionData): List<MissionInfoEntry> {
    var formattedMissions: List<MissionInfoEntry> = emptyList()

    missionInfo.missions.forEach { mission ->
        if (mission.identifier.isNotBlank()) {
            formattedMissions = formattedMissions.plus(
                MissionInfoEntry(
                    secondsRemaining = if (mission.secondsRemaining >= 0) mission.secondsRemaining else 0.0,
                    missionDuration = mission.durationSeconds,
                    date = Instant.now().epochSecond,
                    shipId = mission.ship.number,
                    capacity = mission.capacity,
                    shipLevel = mission.level,
                    targetArtifact = mission.targetArtifact.number,
                    durationType = mission.durationType.number
                )
            )
        }
    }

    return formattedMissions
}

fun createCircularProgressBarBitmap(progress: Float, durationType: Int, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    paint.color = android.graphics.Color.parseColor("#464646")
    val radius = size / 2f - paint.strokeWidth / 2
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    paint.color = getMissionColor(durationType)
    val sweepAngle = 360 * progress
    canvas.drawArc(
        paint.strokeWidth / 2,
        paint.strokeWidth / 2,
        size - paint.strokeWidth / 2,
        size - paint.strokeWidth / 2,
        90f,
        sweepAngle,
        false,
        paint
    )

    return bitmap
}

fun getShipName(shipId: Int): String {
    return ALL_SHIPS[shipId]
}

fun getMissionColor(durationType: Int): Int {
    return when (durationType) {
        //short
        0 -> android.graphics.Color.BLUE
        //standard
        1 -> android.graphics.Color.rgb(160, 32, 240) //purple
        //extended
        2 -> android.graphics.Color.rgb(255, 165, 0) //orange
        //tutorial
        else -> android.graphics.Color.WHITE
    }
}