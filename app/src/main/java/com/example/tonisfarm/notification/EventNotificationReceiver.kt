package com.example.tonisfarm.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tonisfarm.data.local.AppDatabase
import com.example.tonisfarm.domain.model.Evento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EventNotificationReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NOTIFY_EVENT) {
            val eventoId = intent.getLongExtra(EXTRA_EVENTO_ID, -1)
            if (eventoId != -1L) {
                scope.launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        val eventoEntity = database.eventoDao().getEventoById(eventoId)
                        eventoEntity?.let { entity ->
                            val evento = Evento.fromEntity(entity)
                            // Verificar se o alerta ainda é válido (não passou muito tempo)
                            val alertTime = evento.dataAlerta?.time ?: return@let
                            val currentTime = System.currentTimeMillis()
                            // Notificar se está dentro de uma janela de 1 hora
                            if (alertTime <= currentTime && alertTime >= currentTime - 3600000) {
                                NotificationHelper.showEventNotification(context, evento)
                            }
                        }
                    } catch (e: Exception) {
                        // Falha ao carregar evento, mas não quebra
                    }
                }
            }
        }
    }
}

