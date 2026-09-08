package com.example.tonisfarm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vermifugacoes",
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
data class VermifugacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cowId: Long,
    val drugName: String,
    val date: Long // Timestamp
)

