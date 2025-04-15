package tools

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Reminders
import androidx.core.content.ContextCompat
import data.ALL_SHIPS
import data.CalendarEntry
import data.ContractData
import data.ContractInfoEntry
import data.FuelLevelInfo
import data.MissionData
import data.MissionInfoEntry
import data.TANK_SIZES
import data.TankInfo
import ei.Ei.Egg
import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

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

fun formatContractData(contractInfo: ContractData): List<ContractInfoEntry> {
    var formattedContracts: List<ContractInfoEntry> = emptyList()

    contractInfo.contracts.forEach { contract ->
        formattedContracts = formattedContracts.plus(
            ContractInfoEntry(
                eggId = contract.contract.egg.number,
                customEggId = contract.contract.customEggId,
                contractName = contract.contract.name
            )
        )
    }

    return formattedContracts
}

fun getTankCapacity(tankLevel: Int): Long {
    return TANK_SIZES[tankLevel]
}

fun formatTankInfo(missionInfo: MissionData): TankInfo {
    var formattedFuelLevels: List<FuelLevelInfo> = emptyList()

    missionInfo.artifacts.tankFuelsList.forEachIndexed { index, fuel ->
        if (fuel > 0) {
            formattedFuelLevels = formattedFuelLevels.plus(
                FuelLevelInfo(
                    eggId = index + 1,
                    fuelQuantity = fuel,
                    fuelSlider = missionInfo.artifacts.tankLimitsList[index]
                )
            )
        }
    }

    return TankInfo(
        level = missionInfo.artifacts.tankLevel,
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

fun createContractCircularProgressBarBitmap(
    progress: Float,
    size: Int
): Bitmap {
    val color = "#16ac00".toColorInt()
    return createCircularProgressBarBitmap(progress, color, size, 4f)
}

private fun createCircularProgressBarBitmap(
    progress: Float,
    color: Int,
    size: Int,
    width: Float
): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = width
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    paint.color = "#464646".toColorInt()
    val radius = size / 2f - paint.strokeWidth / 2
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    paint.color = color
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

fun bitmapResize(image: Bitmap): Bitmap {
    val width = image.width
    val height = image.height
    val aspectRatio = width / height
    val newWidth = 100
    return if (width > newWidth) {
        val newHeight = newWidth * aspectRatio
        image.scale(newWidth, newHeight, false)
    } else {
        image
    }
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

fun getAsset(assetManager: AssetManager, path: String): InputStream {
    return try {
        assetManager.open(path)
    } catch (_: Exception) {
        assetManager.open("eggs/egg_unknown.png")
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

fun hasCalendarPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.WRITE_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED
}

fun scheduleCalendarEvents(
    context: Context,
    missions: List<MissionInfoEntry>,
    eiUserName: String,
    selectedCalendar: CalendarEntry
) {
    if (hasCalendarPermissions(context)) {
        missions.map { mission ->
            val missionEndTime = getMissionEndTimeMilliseconds(mission)
            if (missionEndTime > 0 && mission.identifier.isNotBlank() && selectedCalendar.id.toInt() != -1 && !hasEvent(
                    context,
                    mission.identifier,
                    missionEndTime
                )
            ) {
                val missionEndTimePlusMinute = missionEndTime + (60 * 1000)
                val eventValues = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, missionEndTime)
                    put(CalendarContract.Events.DTEND, missionEndTimePlusMinute)
                    put(CalendarContract.Events.TITLE, "Ship returning for $eiUserName")
                    put(CalendarContract.Events.DESCRIPTION, mission.identifier)
                    put(CalendarContract.Events.CALENDAR_ID, selectedCalendar.id)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }
                val eventUri: Uri? =
                    context.contentResolver.insert(
                        CalendarContract.Events.CONTENT_URI,
                        eventValues
                    )
                val eventID: Long = eventUri?.lastPathSegment!!.toLong()

                // Get rid of any default reminders that are automatically added with events
                removeDefaultReminders(context, eventID)

                // Add our own reminder at the time of the event
                val reminderValues = ContentValues().apply {
                    put(Reminders.MINUTES, 0)
                    put(Reminders.EVENT_ID, eventID)
                    put(Reminders.METHOD, Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(
                    Reminders.CONTENT_URI,
                    reminderValues
                )
            }
        }
    }
}

private fun removeDefaultReminders(context: Context, eventId: Long) {
    val selection = "${Reminders.EVENT_ID} = ?"
    val selectionArgs = arrayOf(eventId.toString())

    context.contentResolver.delete(Reminders.CONTENT_URI, selection, selectionArgs)
}

private fun hasEvent(context: Context, identifier: String, eventTime: Long): Boolean {
    val uri = CalendarContract.Events.CONTENT_URI

    val projection = arrayOf(
        CalendarContract.Events._ID
    )

    // Look for events within +/- 5 min of event time to reduce query scope
    val startTime = eventTime - (5 * 60 * 1000)
    val endTime = eventTime + (5 * 60 * 1000)

    val selection =
        "${CalendarContract.Events.DESCRIPTION} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
    val selectionArgs = arrayOf(identifier, startTime.toString(), endTime.toString())

    val contentResolver: ContentResolver = context.contentResolver

    val cursor = contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        null
    )

    val hasEvent = cursor?.moveToFirst() == true

    cursor?.close()
    return hasEvent
}
