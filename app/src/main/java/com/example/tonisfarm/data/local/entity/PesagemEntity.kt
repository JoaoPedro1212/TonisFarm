package com.example.tonisfarm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pesagens",
    foreignKeys = [
        ForeignKey(
            entity = CowEntity::class,
            parentColumns = ["id"],
            childColumns = ["cowId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cowId"])]
)
data class PesagemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cowId: Long,
    val weightKg: Double,
    val date: Long // Timestamp
)

