package com.example.tonisfarm.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.domain.model.Evento
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EventNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventoRepository: EventoRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val eventos = eventoRepository.getAllEventos().first()

            eventos.forEach { evento ->
                val alertTime = evento.dataAlerta?.time ?: return@forEach
                // Notificar se o alerta está entre agora e 1 hora no futuro
                if (alertTime >= currentTime && alertTime <= currentTime + 3600000) {
                    NotificationHelper.showEventNotification(applicationContext, evento)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

