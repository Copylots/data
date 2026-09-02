package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DataRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DataRecordDao {
    @Query("SELECT * FROM data_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DataRecord>>

    @Query("SELECT * FROM data_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Int): DataRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DataRecord): Long

    @Update
    suspend fun updateRecord(record: DataRecord)

    @Delete
    suspend fun deleteRecord(record: DataRecord)

    @Query("DELETE FROM data_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
}
