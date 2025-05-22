package tools.utilities

import android.graphics.Bitmap
import data.ALL_SHIPS
import data.FuelLevelInfo
import data.MissionData
import data.MissionInfoEntry
import data.TANK_SIZES
import data.TankInfo
import ei.Ei
import ei.Ei.Egg
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.plus

fun getMissionPercentComplete(
    missionDuration: Double,
    timeRemaining: Double,
    savedTime: Long
): Float {
    var newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    if (newTimeRemaining <= 0) newTimeRemaining = 0.0
    return ((missionDuration - newTimeRemaining) / missionDuration).toFloat()
}

// Returns Pair<timeText, isMoreThanOneDay>
fun getMissionDurationRemaining(
    timeRemaining: Double,
    savedTime: Long,
    useAbsoluteTime: Boolean,
    useAbsoluteTimePlusDay: Boolean,
    use24HrFormat: Boolean
): Pair<String, Boolean> {
    val newTimeRemaining = timeRemaining - (Instant.now().epochSecond - savedTime)
    return if (newTimeRemaining <= 0) {
        Pair("Finished", false)
    } else {
        val days = newTimeRemaining / 86400
        val timeText = if (useAbsoluteTime && (useAbsoluteTimePlusDay || days < 1)) {
            val currentTime = LocalDateTime.now()
            val endingTime = currentTime.plusSeconds(newTimeRemaining.toLong())
            if (use24HrFormat) {
                endingTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                endingTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            }
        } else {
            val hoursMinusDays = (newTimeRemaining % 86400) / 3600
            val hours = newTimeRemaining / 3600
            val minutes = (newTimeRemaining % 3600) / 60

            if (days > 1) {
                if (hoursMinusDays.toInt() == 0) {
                    "${days.toInt()}d"
                } else {
                    "${days.toInt()}d ${hoursMinusDays.toInt()}h"
                }
            } else {
                if (hours.toInt() == 0) {
                    "${minutes.toInt()}m"
                } else {
                    "${hours.toInt()}h ${minutes.toInt()}m"
                }
            }
        }

        Pair(timeText, days >= 1)
    }
}

fun getMissionEndTimeMilliseconds(mission: MissionInfoEntry): Long {
    val newTimeRemaining = mission.secondsRemaining - (Instant.now().epochSecond - mission.date)
    if (newTimeRemaining <= 0) {
        // If the end time is in the past, don't bother returning an actual end time because
        // we won't schedule events in the past
        return 0
    }

    val currentTime = LocalDateTime.now()
    val endingTime = currentTime.plusSeconds(newTimeRemaining.toLong())
    return endingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun formatMissionData(missionInfo: MissionData, backup: Ei.Backup): List<MissionInfoEntry> {
    var formattedMissions: List<MissionInfoEntry> = emptyList()
    var missionsWithFueling = missionInfo.missions
    val fuelingMission = backup.artifactsDb.fuelingMission

    if (fuelingMission.capacity > 0) {
        missionsWithFueling = missionsWithFueling + fuelingMission
    }

    missionsWithFueling.forEach { mission ->
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

fun updateFuelingMission(
    missions: List<MissionInfoEntry>,
    backup: Ei.Backup
): List<MissionInfoEntry> {
    var activeMissions = missions.filter { mission -> mission.identifier.isNotBlank() }
    val fuelingMission = backup.artifactsDb.fuelingMission

    if (fuelingMission.capacity > 0) {
        activeMissions = activeMissions.plus(
            MissionInfoEntry(
                secondsRemaining = if (fuelingMission.secondsRemaining >= 0) fuelingMission.secondsRemaining else 0.0,
                missionDuration = fuelingMission.durationSeconds,
                date = Instant.now().epochSecond,
                shipId = fuelingMission.ship.number,
                capacity = fuelingMission.capacity,
                shipLevel = fuelingMission.level,
                targetArtifact = fuelingMission.targetArtifact.number,
                durationType = fuelingMission.durationType.number,
                identifier = fuelingMission.identifier
            )
        )
    }

    return activeMissions
}

fun getTankCapacity(tankLevel: Int): Long {
    return TANK_SIZES[tankLevel]
}

fun formatTankInfo(backup: Ei.Backup): TankInfo {
    var formattedFuelLevels: List<FuelLevelInfo> = emptyList()

    backup.artifacts.tankFuelsList.forEachIndexed { index, fuel ->
        if (fuel > 0) {
            formattedFuelLevels = formattedFuelLevels.plus(
                FuelLevelInfo(
                    eggId = index + 1,
                    fuelQuantity = fuel,
                    fuelSlider = backup.artifacts.tankLimitsList[index]
                )
            )
        }
    }

    return TankInfo(
        level = backup.artifacts.tankLevel,
        fuelLevels = formattedFuelLevels
    )
}

// Used to help show fuel tanks in the large widget
// We only want the fuel list to show in the last slot
// If the last slot is a fueling ship, replace that entry with the fuel mission
// Otherwise, add a fuel mission to the end of the existing mission list
fun getMissionsWithFuelTank(missions: List<MissionInfoEntry>): List<MissionInfoEntry> {
    var missionsCopy = missions.toList()
    val fuelMission = MissionInfoEntry(
        secondsRemaining = 0.0,
        missionDuration = 0.0,
        date = 0,
        shipId = 0,
        capacity = 0,
        shipLevel = 0,
        targetArtifact = 0,
        durationType = 0,
        identifier = "fuelTankMission"
    )

    missionsCopy.forEach { mission ->
        if (mission.identifier.isBlank()) {
            mission.identifier = "fuelTankMission"
        }
    }

    if (!missionsCopy.any { mission -> mission.identifier == "fuelTankMission" }) {
        missionsCopy = missionsCopy + fuelMission
    }

    return missionsCopy
}

// When using the large widget, if you are not showing tanks and have an odd number of missions (1 or 3)
// OR you are showing tanks and have an even number of missions (2)
// the spacing gets really weird. So add an empty mission to take up the extra space
fun getMissionsWithBlankMission(missions: List<MissionInfoEntry>): List<MissionInfoEntry> {
    val blankMission = MissionInfoEntry(
        secondsRemaining = 0.0,
        missionDuration = 0.0,
        date = 0,
        shipId = 0,
        capacity = 0,
        shipLevel = 0,
        targetArtifact = 0,
        durationType = 0,
        identifier = "blankMission"
    )

    return missions + blankMission
}

fun createMissionCircularProgressBarBitmap(
    progress: Float,
    durationType: Int,
    size: Int,
    isFueling: Boolean
): Bitmap {
    val color = getMissionColor(durationType, isFueling)
    return createCircularProgressBarBitmap(progress, color, size, 8f)
}

fun getShipName(shipId: Int): String {
    return ALL_SHIPS[shipId]
}

fun getEggName(eggId: Int): String {
    val eggName = Egg.forNumber(eggId)?.name?.lowercase()
    return if (eggName.isNullOrBlank()) {
        "egg_unknown"
    } else {
        "egg_$eggName"
    }
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

fun getFuelPercentFilled(capacity: Long, fuelQuantity: Double): Float {
    if (capacity == 0L) return 0f
    return fuelQuantity.toFloat() / capacity.toFloat()
}

fun getFuelAmount(fuelQuantity: Double): String {
    var number = fuelQuantity
    var unit = ""

    if (number < 1000) {
        return number.toInt().toString()
    }

    var units = arrayOf(
        "k",
        "M",
        "B",
        "T",
        "q",
        "Q",
        "s",
        "S",
        "o",
        "N",
        "d",
        "U",
        "D",
        "Td",
        "qd",
        "Qd",
        "sd",
        "Sd",
        "Od",
        "Nd",
        "V",
        "uV",
        "dV",
        "tV",
        "qV",
        "QV",
        "sV",
        "SV",
        "OV",
        "NV",
        "tT"
    )

    while (number >= 1000) {
        number /= 1000
        unit = units.first()
        units = units.drop(1).toTypedArray()
    }

    var formatted = String.format(Locale.ROOT, "%.3g", number)

    // If using %.3g for the string format it _sometimes_ ends up in sci. notation
    // It seems to only be cases where it's right below the threshold of the next power of ten
    // e.g. 999,999,999,999.999999 becomes 1.00+e03B
    // In this case, strip off the +e03 and bump the unit to the next tier letter

    if (formatted.contains("e+")) {
        val split = formatted.split("e+")
        val unitIndex = units.indexOf(unit)
        unit = units[unitIndex + 1]
        formatted = split[0]
    }

    return "$formatted$unit"
}