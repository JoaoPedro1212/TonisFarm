package com.example.tonisfarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tonisfarm.MainActivity
import com.example.tonisfarm.domain.model.TipoEventoExtensions.getDisplayName

object NotificationHelper {
    private const val CHANNEL_ID = "tonisfarm_events_channel"
    private const val CHANNEL_NAME = "Eventos da Fazenda"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de eventos importantes da fazenda"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        eventoId: Long
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventoId", eventoId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun showEventNotification(
        context: Context,
        evento: com.example.tonisfarm.domain.model.Evento
    ) {
        val title = "Evento: ${evento.tipoEvento.getDisplayName()}"
        val descricao = evento.descricao ?: ""
        val message = if (descricao.isNotBlank()) {
            "$descricao\nData: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(evento.dataEvento)}"
        } else {
            "Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(evento.dataEvento)}"
        }
        
        showNotification(
            context = context,
            notificationId = evento.id.toInt(),
            title = title,
            message = message,
            eventoId = evento.id
        )
    }
}

