package com.example.tonisfarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tonisfarm.data.local.dao.CowDao
import com.example.tonisfarm.data.local.dao.EventoDao
import com.example.tonisfarm.data.local.dao.FinancialTransactionDao
import com.example.tonisfarm.data.local.dao.VacinacaoDao
import com.example.tonisfarm.data.local.dao.VermifugacaoDao
import com.example.tonisfarm.data.local.dao.PesagemDao
import com.example.tonisfarm.data.local.dao.PartoDao
import com.example.tonisfarm.data.local.entity.CowEntity
import com.example.tonisfarm.data.local.entity.EventoEntity
import com.example.tonisfarm.data.local.entity.EventCowCrossRef
import com.example.tonisfarm.data.local.entity.FinancialTransactionEntity
import com.example.tonisfarm.data.local.entity.VacinacaoEntity
import com.example.tonisfarm.data.local.entity.VermifugacaoEntity
import com.example.tonisfarm.data.local.entity.PesagemEntity
import com.example.tonisfarm.data.local.entity.PartoEntity

@Database(
    entities = [
        CowEntity::class, 
        EventoEntity::class, 
        FinancialTransactionEntity::class, 
        EventCowCrossRef::class,
        VacinacaoEntity::class,
        VermifugacaoEntity::class,
        PesagemEntity::class,
        PartoEntity::class
    ],
    version = 11, // Incrementado para adicionar campos de recorrência nas transações financeiras
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cowDao(): CowDao
    abstract fun eventoDao(): EventoDao
    abstract fun eventCowCrossRefDao(): com.example.tonisfarm.data.local.dao.EventCowCrossRefDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun vacinacaoDao(): VacinacaoDao
    abstract fun vermifugacaoDao(): VermifugacaoDao
    abstract fun pesagemDao(): PesagemDao
    abstract fun partoDao(): PartoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tonisfarm_database"
                )
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tipo TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        valor REAL NOT NULL,
                        data INTEGER NOT NULL,
                        descricao TEXT
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE cows ADD COLUMN dataInicioPrenhez INTEGER
                """.trimIndent())
            }
        }
        
                val MIGRATION_3_4 = object : Migration(3, 4) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // Criar nova tabela de eventos sem cowId
                        database.execSQL("""
                            CREATE TABLE IF NOT EXISTS eventos_new (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                tipoEvento TEXT NOT NULL,
                                dataEvento INTEGER NOT NULL,
                                descricao TEXT,
                                dataAlerta INTEGER
                            )
                        """.trimIndent())

                        // Migrar dados antigos (usar primeiro cowId encontrado)
                        database.execSQL("""
                            INSERT INTO eventos_new (id, tipoEvento, dataEvento, descricao, dataAlerta)
                            SELECT id, tipoEvento, dataEvento, descricao, dataAlerta FROM eventos
                        """.trimIndent())

                        // Criar tabela de junção
                        database.execSQL("""
                            CREATE TABLE IF NOT EXISTS event_cow_cross_ref (
                                eventoId INTEGER NOT NULL,
                                cowId INTEGER NOT NULL,
                                PRIMARY KEY(eventoId, cowId),
                                FOREIGN KEY(eventoId) REFERENCES eventos_new(id) ON DELETE CASCADE,
                                FOREIGN KEY(cowId) REFERENCES cows(id) ON DELETE CASCADE
                            )
                        """.trimIndent())

                        // Migrar associações antigas
                        database.execSQL("""
                            INSERT INTO event_cow_cross_ref (eventoId, cowId)
                            SELECT id, cowId FROM eventos WHERE cowId IS NOT NULL
                        """.trimIndent())

                        // Remover tabela antiga e renomear
                        database.execSQL("DROP TABLE eventos")
                        database.execSQL("ALTER TABLE eventos_new RENAME TO eventos")
                    }
                }

                val MIGRATION_5_6 = object : Migration(5, 6) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("""
                            ALTER TABLE cows ADD COLUMN ultimaVacinacaoManual INTEGER
                        """.trimIndent())
                        database.execSQL("""
                            ALTER TABLE cows ADD COLUMN ultimaVermifugacaoManual INTEGER
                        """.trimIndent())
                        database.execSQL("""
                            ALTER TABLE cows ADD COLUMN ultimaPesagemManual INTEGER
                        """.trimIndent())
                        database.execSQL("""
                            ALTER TABLE cows ADD COLUMN ultimoPartoManual INTEGER
                        """.trimIndent())
                    }
                }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN horaEvento INTEGER
                """.trimIndent())
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Criar tabela de vacinações
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS vacinacoes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cowId INTEGER NOT NULL,
                        vaccineName TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(cowId) REFERENCES cows(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_vacinacoes_cowId ON vacinacoes(cowId)")

                // Criar tabela de vermifugações
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS vermifugacoes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cowId INTEGER NOT NULL,
                        drugName TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(cowId) REFERENCES cows(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_vermifugacoes_cowId ON vermifugacoes(cowId)")

                // Criar tabela de pesagens
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pesagens (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cowId INTEGER NOT NULL,
                        weightKg REAL NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(cowId) REFERENCES cows(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_pesagens_cowId ON pesagens(cowId)")

                // Criar tabela de partos
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS partos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cowId INTEGER NOT NULL,
                        calvesCount INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(cowId) REFERENCES cows(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_partos_cowId ON partos(cowId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN vaccineType TEXT
                """.trimIndent())
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN dewormingType TEXT
                """.trimIndent())
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN weightKg REAL
                """.trimIndent())
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN calvesCount INTEGER
                """.trimIndent())
                database.execSQL("""
                    ALTER TABLE eventos ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0
                """.trimIndent())
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE financial_transactions ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'NONE'
                """.trimIndent())
                database.execSQL("""
                    ALTER TABLE financial_transactions ADD COLUMN nextOccurrenceDate INTEGER
                """.trimIndent())
            }
        }
    }
}

