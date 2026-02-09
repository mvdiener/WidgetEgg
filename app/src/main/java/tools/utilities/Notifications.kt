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
import data.CONTRACT_NOTIFICATION_CHANNEL_ID
import data.PeriodicalsContractInfoEntry

fun hasNotificationPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        return true
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
    periodicalsContracts: List<PeriodicalsContractInfoEntry>
): List<PeriodicalsContractInfoEntry> {
    if (!hasNotificationPermissions(context)) return periodicalsContracts

    return periodicalsContracts.map { contract ->
        if (contract.notificationSent || contract.identifier == "first-contract") return@map contract
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
                // notificationId is a unique int for each notification that you want to be able to update
                notify(contract.identifier.hashCode(), builder.build())
            }

            contract.copy(notificationSent = true)
        }
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