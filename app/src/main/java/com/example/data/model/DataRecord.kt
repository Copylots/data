package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_records")
data class DataRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val tempatLahir: String,
    val tanggalLahir: String,
    val jenisKelamin: String,
    val alamatLengkap: String,
    val agama: String,
    val statusPerkawinan: String,
    val kewarganegaraan: String,
    val timestamp: Long = System.currentTimeMillis()
)
