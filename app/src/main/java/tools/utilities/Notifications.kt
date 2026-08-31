package tools.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.widgetegg.widgeteggapp.R
import data.constants.CONTRACT_NOTIFICATION_CHANNEL_ID
import data.ContractInfoEntry
import data.Event
import data.PeriodicalsContractInfoEntry
import data.VirtueInfo

fun hasNotificationPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun createNotificationChannel(context: Context) {
    val name = "WidgetEgg Contract Notifications"
    val descriptionText = "Notifications for contracts"
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    val channel = NotificationChannel(CONTRACT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun sendContractNotification(
    context: Context,
    newContractNotifications: Boolean,
    incompleteContractNotifications: Boolean,
    contracts: List<ContractInfoEntry>,
    periodicalsContracts: List<PeriodicalsContractInfoEntry>
): List<PeriodicalsContractInfoEntry> {
    if (!hasNotificationPermissions(context)) return periodicalsContracts

    // Filter out active contracts
    val filtered = periodicalsContracts.filter { periodical ->
        (periodical.identifier !in contracts.map { it.identifier })
    }

    return filtered.map { contract ->
        if (contract.notificationSent) return@map contract
        val numOfGoalsAchieved = contract.archivedContractInfo?.numOfGoalsAchieved ?: 0
        val lastScore = contract.archivedContractInfo?.lastScore ?: 0.0

        val isNewContract = numOfGoalsAchieved == 0 && lastScore == 0.0
        val isAwaitingRetry = numOfGoalsAchieved < contract.goals.size && lastScore > 0.0

        val shouldNotifyNew = isNewContract && newContractNotifications
        val shouldNotifyIncomplete = isAwaitingRetry && incompleteContractNotifications

        if (!shouldNotifyNew && !shouldNotifyIncomplete) {
            contract
        } else {
            val titleText = if (shouldNotifyNew) {
                "New contract available"
            } else {
                "Contract awaiting retry"
            }

            val builder = createNotificationBuilder(context, titleText, contract.name)
            with(NotificationManagerCompat.from(context)) {
                notify(contract.identifier.hashCode(), builder.build())
            }

            contract.copy(notificationSent = true)
        }
    }
}

fun sendEventNotification(
    context: Context,
    virtueInfo: VirtueInfo,
    selectedEventNotifications: Set<String>,
    sentEventNotificationMap: Map<String, Long>
): Map<String, Long> {
    if (!hasNotificationPermissions(context) || selectedEventNotifications.isEmpty()) return sentEventNotificationMap

    // Prune entries older than 2 weeks to keep the datastore small
    val currentTime = System.currentTimeMillis()
    val twoWeeksAgo = currentTime - (14L * 24 * 60 * 60 * 1000)
    val updatedNotificationMap =
        sentEventNotificationMap.filter { it.value > twoWeeksAgo }.toMutableMap()

    // Find intersection of today's events and the events where the user wants a notification
    val matchingEvents = virtueInfo.dailyEvents.filter { event ->
        valueSpecificEventMatches(
            event,
            selectedEventNotifications
        ) && event.identifier !in updatedNotificationMap
    }

    if (matchingEvents.isEmpty()) return updatedNotificationMap

    val titleText = if (matchingEvents.size == 1) "New event available" else "New events available"
    val messageText = matchingEvents.joinToString(separator = ", ") {
        val name = it.name
        val multiplierText = if (it.multiplier > 1) {
            " ${(it.multiplier).toInt()}x"
        } else if (it.multiplier == 1.0) {
            ""
        } else {
            val percent = ((1.0 - it.multiplier) * 100).toInt()
            " $percent% off"
        }
        val ultraText = if (it.isUltra) {
            " (Ultra Only)"
        } else {
            ""
        }

        "$name$multiplierText$ultraText"
    }

    val builder = createNotificationBuilder(context, titleText, messageText)
    with(NotificationManagerCompat.from(context)) {
        // Using a specific ID for events to avoid overwriting event notifications
        notify("event_notifications".hashCode(), builder.build())
    }

    // Add new events to history
    matchingEvents.forEach { event ->
        updatedNotificationMap[event.identifier] = currentTime
    }

    return updatedNotificationMap
}

private fun valueSpecificEventMatches(event: Event, selected: Set<String>): Boolean {
    if (selected.contains(event.type)) return true

    return when (event.type) {
        "prestige-boost" -> selected.contains("prestige-boost-3x") && event.multiplier >= 3.0
        else -> false
    }
}

private fun createNotificationBuilder(
    context: Context,
    titleText: String,
    messageText: String
): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, CONTRACT_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(titleText)
        .setContentText(messageText)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
}