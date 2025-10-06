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
                    missionEndTime
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