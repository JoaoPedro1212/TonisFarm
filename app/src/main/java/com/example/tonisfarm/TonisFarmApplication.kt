package com.example.tonisfarm

import android.app.Application
import com.example.tonisfarm.data.local.AppDatabase
import com.example.tonisfarm.data.repository.RecurrenceProcessor
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.notification.NotificationHelper
import com.example.tonisfarm.notification.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class TonisFarmApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        NotificationScheduler.schedulePeriodicCheck(this)
        NotificationScheduler.scheduleHistoryUpdate(this)
        
        // Processar eventos que já passaram e reagendar os futuros
        applicationScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@TonisFarmApplication)
                val eventos = database.eventoDao().getAllEventosIncludingCompleted().first()
                
                eventos.forEach { eventoEntity ->
                    val evento = Evento.fromEntity(eventoEntity)
                    if (!evento.isCompleted) {
                        // Reagendar conclusão (se já passou, será processado imediatamente)
                        NotificationScheduler.scheduleEventCompletion(this@TonisFarmApplication, evento)
                    }
                }
            } catch (e: Exception) {
                // Falha ao processar, mas não quebra o app
            }
        }
        
        // Processar recorrências financeiras na inicialização
        applicationScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@TonisFarmApplication)
                val recurrenceProcessor = RecurrenceProcessor(database.financialTransactionDao())
                recurrenceProcessor.processRecurrences()
            } catch (e: Exception) {
                // Falha ao processar, mas não quebra o app
            }
        }
        
        // Também processar imediatamente eventos que já passaram
        NotificationScheduler.triggerHistoryUpdateNow(this)
    }
}

