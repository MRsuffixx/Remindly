package com.mrsuffix.remindly.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mrsuffix.remindly.MainActivity
import com.mrsuffix.remindly.R
import com.mrsuffix.remindly.RemindlyApplication
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepository: EventRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val events = eventRepository.getAllEvents().first()
            
            events.forEach { event ->
                if (event.isActive) {
                    val daysUntil = event.daysUntilNext()
                    if (event.reminderDays.contains(daysUntil)) {
                        showNotification(event, daysUntil)
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun showNotification(event: Event, daysUntil: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = when (daysUntil) {
            0 -> "🎉 Bugün: ${event.name}"
            1 -> "⏰ Yarın: ${event.name}"
            else -> "📅 $daysUntil gün sonra: ${event.name}"
        }
        
        val message = buildString {
            append(event.eventCategory.emoji)
            append(" ")
            append(event.eventCategory.displayName)
            if (event.note.isNotBlank()) {
                append("\n")
                append(event.note)
            }
        }
        
        val notification = NotificationCompat.Builder(context, RemindlyApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(event.id.toInt(), notification)
        }
    }
    
    companion object {
        const val WORK_NAME = "reminder_worker"
        
        fun schedule(context: Context, notificationTime: LocalTime = LocalTime.of(9, 0)) {
            val now = LocalDateTime.now()
            var scheduledTime = now.toLocalDate().atTime(notificationTime)
            
            if (now >= scheduledTime) {
                scheduledTime = scheduledTime.plusDays(1)
            }
            
            val delay = Duration.between(now, scheduledTime).toMillis()
            
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
        
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
