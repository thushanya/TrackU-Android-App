package com.example.labexam3.utils

import android.content.Context
import androidx.work.*
import com.example.labexam3.models.HydrationReminder
import com.example.labexam3.workers.HydrationReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility class for scheduling hydration reminders using WorkManager
 */
object ReminderScheduler {

    /**
     * Schedule all enabled hydration reminders
     */
    fun scheduleReminders(context: Context, reminders: List<HydrationReminder>) {
        // Cancel all existing reminders first
        WorkManager.getInstance(context).cancelAllWorkByTag("hydration_reminder")

        // Schedule each enabled reminder
        reminders.filter { it.enabled }.forEach { reminder ->
            scheduleReminder(context, reminder)
        }
    }

    /**
     * Schedule a single hydration reminder
     */
    private fun scheduleReminder(context: Context, reminder: HydrationReminder) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
        }

        // If the target time has passed today, schedule for tomorrow
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = targetTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("hydration_reminder")
            .addTag("reminder_${reminder.hour}_${reminder.minute}")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Cancel a specific reminder
     */
    fun cancelReminder(context: Context, reminder: HydrationReminder) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("reminder_${reminder.hour}_${reminder.minute}")
    }

    /**
     * Cancel all reminders
     */
    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("hydration_reminder")
    }
}
