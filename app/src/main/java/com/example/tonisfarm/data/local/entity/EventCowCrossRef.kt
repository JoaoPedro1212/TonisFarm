package com.example.tonisfarm.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "event_cow_cross_ref",
    primaryKeys = ["eventoId", "cowId"]
)
data class EventCowCrossRef(
    val eventoId: Long,
    val cowId: Long
)

