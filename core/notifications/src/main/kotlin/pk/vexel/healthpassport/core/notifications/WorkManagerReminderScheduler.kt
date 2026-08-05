package pk.vexel.healthpassport.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Data
import java.util.concurrent.TimeUnit
import pk.vexel.healthpassport.core.database.DatabaseProvider

class WorkManagerReminderScheduler(private val context: Context) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context)

    override suspend fun schedule(id: String, dueAtEpochMillis: Long, recurrence: String) {
        ensureChannel()
        val delay = (dueAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val input = Data.Builder().putString(ReminderWorker.REMINDER_ID, id).build()
        if (recurrence == "DAILY") {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(input).setConstraints(Constraints.Builder().setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED).build()).build()
            workManager.enqueueUniquePeriodicWork(id, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            val request = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(input).build()
            workManager.enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
        }
    }

    override suspend fun cancel(id: String) { workManager.cancelUniqueWork(id) }

    override suspend fun reconcile() {
        val database = DatabaseProvider.create(context)
        try {
            database.reminderDao().findScheduled().forEach { reminder ->
                schedule(reminder.id, reminder.dueAtEpochMillis, reminder.recurrence)
            }
        } finally {
            database.close()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "Health reminders", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "User-created reminders" })
        }
    }

    companion object { const val CHANNEL_ID = "health_reminders" }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(REMINDER_ID) ?: return Result.failure()
        val database = DatabaseProvider.create(applicationContext)
        val reminder = database.reminderDao().find(id) ?: return Result.failure()
        if (reminder.status != "SCHEDULED") return Result.success()
        val notification = NotificationCompat.Builder(applicationContext, WorkManagerReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Vexel Health Passport reminder")
            .setContentText("You have a reminder to review")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(applicationContext, 0, applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        val permitted = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (permitted && NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
        if (reminder.recurrence == "ONCE") database.reminderDao().setStatus(id, "MISSED", System.currentTimeMillis())
        database.close()
        return Result.success()
    }
    companion object { const val REMINDER_ID = "reminder_id" }
}
