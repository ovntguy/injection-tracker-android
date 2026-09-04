package com.deeeelay.injectiontracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InjectionLogDao {
    @Query("SELECT * FROM injection_logs ORDER BY loggedAtEpochMillis DESC")
    fun observeAll(): Flow<List<InjectionLogEntity>>

    @Query("SELECT * FROM injection_logs ORDER BY loggedAtEpochMillis DESC")
    suspend fun getAll(): List<InjectionLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InjectionLogEntity)
}
