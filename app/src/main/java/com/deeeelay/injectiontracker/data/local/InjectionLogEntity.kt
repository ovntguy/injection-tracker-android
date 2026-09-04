package com.deeeelay.injectiontracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "injection_logs")
data class InjectionLogEntity(
    @PrimaryKey val id: String,
    val type: String,
    val loggedAtEpochMillis: Long,
    val site: String?,
)
