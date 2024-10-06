package tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import data.MissionData
import data.ALL_SHIPS
import data.MissionInfoEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getMissionPercentComplete(
    missionDuration: Double,
    timeRemaining: Double,
    savedTime: Long
): Float {
    var newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    if (newTimeRemaining <= 0) newTimeRemaining = 0.0
    return ((missionDuration - newTimeRemaining) / missionDuration).toFloat()
}

fun getMissionDurationRemaining(
    timeRemaining: Double,
    savedTime: Long,
    useAbsoluteTime: Boolean
): String {
    val newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    return if (newTimeRemaining <= 0) {
        "Finished!"
    } else {
        val days = newTimeRemaining / 86400
        if (useAbsoluteTime && days < 1) {
            val currentTime = LocalDateTime.now()
            val endingTime = currentTime.plusSeconds(newTimeRemaining.toLong())
            endingTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        } else {
            val hoursMinusDays = (newTimeRemaining % 86400) / 3600
            val hours = newTimeRemaining / 3600
            val minutes = (newTimeRemaining % 3600) / 60

            if (days > 1) {
                if (hoursMinusDays.toInt() == 0) {
                    "${days.toInt()}d"
                } else {
                    "${days.toInt()}d ${hoursMinusDays.toInt()}hr"
                }
            } else {
                if (hours.toInt() == 0) {
                    "${minutes.toInt()}min"
                } else {
                    "${hours.toInt()}hr ${minutes.toInt()}min"
                }
            }
        }
    }
}

fun formatMissionData(missionInfo: MissionData): List<MissionInfoEntry> {
    var formattedMissions: List<MissionInfoEntry> = emptyList()

    missionInfo.missions.forEach { mission ->
        formattedMissions = formattedMissions.plus(
            MissionInfoEntry(
                secondsRemaining = if (mission.secondsRemaining >= 0) mission.secondsRemaining else 0.0,
                missionDuration = mission.durationSeconds,
                date = Instant.now().epochSecond,
                shipId = mission.ship.number,
                capacity = mission.capacity,
                shipLevel = mission.level,
                targetArtifact = mission.targetArtifact.number,
                durationType = mission.durationType.number,
                identifier = mission.identifier
            )
        )
    }

    return formattedMissions
}

fun createCircularProgressBarBitmap(
    progress: Float,
    durationType: Int,
    size: Int,
    isFueling: Boolean
): Bitmap {
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

    paint.color = getMissionColor(durationType, isFueling)
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

fun getMissionColor(durationType: Int, isFueling: Boolean): Int {
    val fuelingAlpha =
        if (isFueling) {
            100
        } else {
            255
        }

    return when (durationType) {
        //short
        0 -> android.graphics.Color.argb(fuelingAlpha, 0, 0, 255) //blue
        //standard
        1 -> android.graphics.Color.argb(fuelingAlpha, 160, 32, 240) //purple
        //extended
        2 -> android.graphics.Color.argb(fuelingAlpha, 255, 165, 0) //orange
        //tutorial
        else -> android.graphics.Color.argb(fuelingAlpha, 255, 255, 255) //white
    }
}