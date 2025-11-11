package tools.utilities

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Reminders
import androidx.core.content.ContextCompat
import data.CalendarEntry
import data.MissionInfoEntry
import java.util.TimeZone

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
    selectedCalendar: CalendarEntry,
    isVirtueMission: Boolean = false
) {
    if (hasCalendarPermissions(context)) {
        missions.map { mission ->
            val missionEndTime = getMissionEndTimeMilliseconds(mission)
            if (missionEndTime > 0 && mission.identifier.isNotBlank() && selectedCalendar.id.toInt() != -1 && !hasEvent(
                    context,
                    mission.identifier,
                    missionEndTime,
                    selectedCalendar.id.toInt()
                )
            ) {
                val missionEndTimePlusMinute = missionEndTime + (60 * 1000)
                val shipTypeText = if (isVirtueMission) "Virtue ship" else "Ship"
                val eventValues = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, missionEndTime)
                    put(CalendarContract.Events.DTEND, missionEndTimePlusMinute)
                    put(CalendarContract.Events.TITLE, "$shipTypeText returning for $eiUserName")
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

fun removeCalendarEvents(context: Context, selectedCalendar: CalendarEntry) {
    if (hasCalendarPermissions(context) && selectedCalendar.id.toInt() != -1) {
        val eventIds = getEventIdsToDelete(context, selectedCalendar.id.toInt())

        if (eventIds.isNotEmpty()) {
            val selection = "${CalendarContract.Events._ID} IN (${eventIds.joinToString(",")})"
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                selection,
                null
            )
        }
    }
}

private fun removeDefaultReminders(context: Context, eventId: Long) {
    val selection = "${Reminders.EVENT_ID} = ?"
    val selectionArgs = arrayOf(eventId.toString())

    context.contentResolver.delete(Reminders.CONTENT_URI, selection, selectionArgs)
}

private fun hasEvent(
    context: Context,
    identifier: String,
    eventTime: Long,
    calendarId: Int
): Boolean {
    val uri = CalendarContract.Events.CONTENT_URI

    val projection = arrayOf(
        CalendarContract.Events._ID
    )

    // Look for events within +/- 2 days of event time to reduce query scope
    val startTime = eventTime - (2 * 24 * 60 * 60 * 1000L)
    val endTime = eventTime + (2 * 24 * 60 * 60 * 1000L)

    val selection =
        "${CalendarContract.Events.DESCRIPTION} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ? AND ${CalendarContract.Events.CALENDAR_ID} = ?"
    val selectionArgs =
        arrayOf(identifier, startTime.toString(), endTime.toString(), calendarId.toString())

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

private fun getEventIdsToDelete(context: Context, calendarId: Int): List<Long> {
    val uri = CalendarContract.Events.CONTENT_URI

    val projection = arrayOf(
        CalendarContract.Events._ID
    )

    val shipDescriptionText = "%hip returning for %"

    // Look for events from a day ago to a month ago to reduce query scope
    val now = System.currentTimeMillis()
    val startTime = now - (30 * 24 * 60 * 60 * 1000L)
    val endTime = now - (24 * 60 * 60 * 1000L)

    val selection =
        "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.TITLE} LIKE ? AND ${CalendarContract.Events.DTEND} BETWEEN ? AND ?"
    val selectionArgs = arrayOf(
        calendarId.toString(),
        shipDescriptionText,
        startTime.toString(),
        endTime.toString()
    )

    val contentResolver: ContentResolver = context.contentResolver

    val cursor = contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        null
    )

    val eventIds = mutableListOf<Long>()
    cursor?.use {
        val idColumnIndex = it.getColumnIndex(CalendarContract.Events._ID)
        while (it.moveToNext()) {
            val eventId = it.getLong(idColumnIndex)
            eventIds.add(eventId)
        }
    }

    return eventIds
}