package com.example.tonisfarm.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import com.example.tonisfarm.domain.model.Evento
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val ACTION_NOTIFY_EVENT = "com.example.tonisfarm.NOTIFY_EVENT"
const val ACTION_COMPLETE_EVENT = "com.example.tonisfarm.COMPLETE_EVENT"
const val EXTRA_EVENTO_ID = "evento_id"

object NotificationScheduler {

    fun scheduleEventNotification(context: Context, evento: Evento) {
        val alertTime = evento.dataAlerta ?: return
        val alertMillis = alertTime.time
        val currentMillis = System.currentTimeMillis()
        
        // Não agendar se já passou
        if (alertMillis <= currentMillis) {
            return
        }

        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            action = ACTION_NOTIFY_EVENT
            putExtra(EXTRA_EVENTO_ID, evento.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            evento.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alertMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alertMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Falha ao agendar, mas não quebra o app
        }
    }

    fun cancelEventNotification(context: Context, eventoId: Long) {
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            action = ACTION_NOTIFY_EVENT
            putExtra(EXTRA_EVENTO_ID, eventoId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun schedulePeriodicCheck(context: Context) {
        // Verificar eventos pendentes a cada 15 minutos usando WorkManager
        // Isso é apenas um fallback, o AlarmManager é o principal
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EventNotificationWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "event_notification_work",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun scheduleHistoryUpdate(context: Context) {
        // Verificar e atualizar histórico de eventos passados a cada 5 minutos
        // Intervalo mínimo do WorkManager é 15 minutos, então usaremos 15 minutos
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EventHistoryUpdateWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "event_history_update",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun triggerHistoryUpdateNow(context: Context) {
        // Executar atualização imediatamente (quando app abre, por exemplo)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<EventHistoryUpdateWorker>()
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun scheduleEventCompletion(context: Context, evento: Evento) {
        val eventDate = evento.dataEvento
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate
        
        // Se o evento tem horário, usar o horário exato
        // Se não tem horário, usar início do dia seguinte
        val completionTime: Long = if (evento.horaEvento != null) {
            val horaEventoCalendar = Calendar.getInstance()
            horaEventoCalendar.time = evento.horaEvento
            eventCalendar.set(Calendar.HOUR_OF_DAY, horaEventoCalendar.get(Calendar.HOUR_OF_DAY))
            eventCalendar.set(Calendar.MINUTE, horaEventoCalendar.get(Calendar.MINUTE))
            eventCalendar.set(Calendar.SECOND, 0)
            eventCalendar.set(Calendar.MILLISECOND, 0)
            eventCalendar.timeInMillis
        } else {
            // Sem horário: agendar para início do dia seguinte
            eventCalendar.add(Calendar.DAY_OF_MONTH, 1)
            eventCalendar.set(Calendar.HOUR_OF_DAY, 0)
            eventCalendar.set(Calendar.MINUTE, 0)
            eventCalendar.set(Calendar.SECOND, 0)
            eventCalendar.set(Calendar.MILLISECOND, 0)
            eventCalendar.timeInMillis
        }
        
        val currentMillis = System.currentTimeMillis()
        
        // Se já passou, processar imediatamente via Worker
        if (completionTime <= currentMillis) {
            triggerHistoryUpdateNow(context)
            return
        }

        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return

        val intent = Intent(context, EventCompletionReceiver::class.java).apply {
            action = ACTION_COMPLETE_EVENT
            putExtra(EXTRA_EVENTO_ID, evento.id)
        }

        // Usar um ID diferente do de notificação para evitar conflitos
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (evento.id + 1000000).toInt(), // Offset para não conflitar com notificações
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    completionTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    completionTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Falha ao agendar, mas não quebra o app
        }
    }

    fun cancelEventCompletion(context: Context, eventoId: Long) {
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return

        val intent = Intent(context, EventCompletionReceiver::class.java).apply {
            action = ACTION_COMPLETE_EVENT
            putExtra(EXTRA_EVENTO_ID, eventoId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (eventoId + 1000000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}

