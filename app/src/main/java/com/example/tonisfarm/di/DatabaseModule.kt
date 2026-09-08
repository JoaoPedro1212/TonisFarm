package com.example.tonisfarm.di

import android.content.Context
import androidx.room.Room
import com.example.tonisfarm.data.local.AppDatabase
import com.example.tonisfarm.data.local.dao.CowDao
import com.example.tonisfarm.data.local.dao.EventoDao
import com.example.tonisfarm.data.local.dao.EventCowCrossRefDao
import com.example.tonisfarm.data.local.dao.FinancialTransactionDao
import com.example.tonisfarm.data.local.dao.VacinacaoDao
import com.example.tonisfarm.data.local.dao.VermifugacaoDao
import com.example.tonisfarm.data.local.dao.PesagemDao
import com.example.tonisfarm.data.local.dao.PartoDao
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.data.repository.CowRepositoryImpl
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.data.repository.EventoRepositoryImpl
import com.example.tonisfarm.data.repository.FinancialTransactionRepository
import com.example.tonisfarm.data.repository.FinancialTransactionRepositoryImpl
import com.example.tonisfarm.data.repository.VacinacaoRepository
import com.example.tonisfarm.data.repository.VacinacaoRepositoryImpl
import com.example.tonisfarm.data.repository.VermifugacaoRepository
import com.example.tonisfarm.data.repository.VermifugacaoRepositoryImpl
import com.example.tonisfarm.data.repository.PesagemRepository
import com.example.tonisfarm.data.repository.PesagemRepositoryImpl
import com.example.tonisfarm.data.repository.PartoRepository
import com.example.tonisfarm.data.repository.PartoRepositoryImpl
import com.example.tonisfarm.data.repository.RecurrenceProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tonisfarm_database"
        )
        .fallbackToDestructiveMigration() // Destrói e recria o banco se houver problemas de migração
        .addMigrations(
            AppDatabase.MIGRATION_1_2, 
            AppDatabase.MIGRATION_2_3, 
            AppDatabase.MIGRATION_3_4, 
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11
        )
        .build()
    }

    @Provides
    fun provideCowDao(database: AppDatabase): CowDao {
        return database.cowDao()
    }

    @Provides
    fun provideEventoDao(database: AppDatabase): EventoDao {
        return database.eventoDao()
    }

    @Provides
    fun provideEventCowCrossRefDao(database: AppDatabase): EventCowCrossRefDao {
        return database.eventCowCrossRefDao()
    }

    @Provides
    fun provideFinancialTransactionDao(database: AppDatabase): FinancialTransactionDao {
        return database.financialTransactionDao()
    }

    @Provides
    fun provideVacinacaoDao(database: AppDatabase): VacinacaoDao {
        return database.vacinacaoDao()
    }

    @Provides
    fun provideVermifugacaoDao(database: AppDatabase): VermifugacaoDao {
        return database.vermifugacaoDao()
    }

    @Provides
    fun providePesagemDao(database: AppDatabase): PesagemDao {
        return database.pesagemDao()
    }

    @Provides
    fun providePartoDao(database: AppDatabase): PartoDao {
        return database.partoDao()
    }

    @Provides
    @Singleton
    fun provideCowRepository(cowDao: CowDao): CowRepository {
        return CowRepositoryImpl(cowDao)
    }

    @Provides
    @Singleton
    fun provideEventoRepository(
        eventoDao: EventoDao,
        eventCowCrossRefDao: EventCowCrossRefDao,
        @ApplicationContext context: Context
    ): EventoRepository {
        return EventoRepositoryImpl(eventoDao, eventCowCrossRefDao, context)
    }

    @Provides
    @Singleton
    fun provideRecurrenceProcessor(
        transactionDao: FinancialTransactionDao
    ): RecurrenceProcessor {
        return RecurrenceProcessor(transactionDao)
    }

    @Provides
    @Singleton
    fun provideFinancialTransactionRepository(
        transactionDao: FinancialTransactionDao,
        recurrenceProcessor: RecurrenceProcessor
    ): FinancialTransactionRepository {
        return FinancialTransactionRepositoryImpl(transactionDao, recurrenceProcessor)
    }

    @Provides
    @Singleton
    fun provideVacinacaoRepository(
        vacinacaoDao: VacinacaoDao
    ): VacinacaoRepository {
        return VacinacaoRepositoryImpl(vacinacaoDao)
    }

    @Provides
    @Singleton
    fun provideVermifugacaoRepository(
        vermifugacaoDao: VermifugacaoDao
    ): VermifugacaoRepository {
        return VermifugacaoRepositoryImpl(vermifugacaoDao)
    }

    @Provides
    @Singleton
    fun providePesagemRepository(
        pesagemDao: PesagemDao
    ): PesagemRepository {
        return PesagemRepositoryImpl(pesagemDao)
    }

    @Provides
    @Singleton
    fun providePartoRepository(
        partoDao: PartoDao
    ): PartoRepository {
        return PartoRepositoryImpl(partoDao)
    }
}

