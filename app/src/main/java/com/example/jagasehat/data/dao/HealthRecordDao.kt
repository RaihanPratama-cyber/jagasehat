package com.example.jagasehat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jagasehat.model.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE ownerUsername = :ownerUsername ORDER BY createdAt DESC")
    fun getRecordsByOwner(ownerUsername: String): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getRecordsByMemberId(memberId: String): Flow<List<HealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord)

    @Query("DELETE FROM health_records WHERE id = :recordId")
    suspend fun deleteById(recordId: String)
}
