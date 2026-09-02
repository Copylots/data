package com.example.data.repository

import com.example.data.local.DataRecordDao
import com.example.data.model.DataRecord
import kotlinx.coroutines.flow.Flow

class DataRecordRepository(private val dataRecordDao: DataRecordDao) {
    val allRecords: Flow<List<DataRecord>> = dataRecordDao.getAllRecords()

    suspend fun getRecordById(id: Int): DataRecord? {
        return dataRecordDao.getRecordById(id)
    }

    suspend fun insertRecord(record: DataRecord): Long {
        return dataRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: DataRecord) {
        dataRecordDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: DataRecord) {
        dataRecordDao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        dataRecordDao.deleteRecordById(id)
    }
}
