package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DataRecord
import com.example.data.repository.DataRecordRepository
import com.example.utils.ExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ExportStatus {
    object Idle : ExportStatus
    object Loading : ExportStatus
    data class Success(val uri: Uri?, val fileName: String) : ExportStatus
    data class Error(val message: String) : ExportStatus
}

class FormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DataRecordRepository
    private val sharedPrefs = application.getSharedPreferences("FormAppPrefs", Context.MODE_PRIVATE)

    // Auth States
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Form inputs state
    val nama = MutableStateFlow("")
    val tempatLahir = MutableStateFlow("")
    val tanggalLahir = MutableStateFlow("")
    val jenisKelamin = MutableStateFlow("Laki-laki")
    val alamatLengkap = MutableStateFlow("")
    val agama = MutableStateFlow("Islam")
    val statusPerkawinan = MutableStateFlow("Belum Kawin")
    val kewarganegaraan = MutableStateFlow("WNI")

    // Edit mode state
    private val _editingRecordId = MutableStateFlow<Int?>(null)
    val editingRecordId: StateFlow<Int?> = _editingRecordId.asStateFlow()

    // Export state
    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()

    // Records List Flow
    val allRecords: StateFlow<List<DataRecord>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DataRecordRepository(database.dataRecordDao())
        
        // Load logged in state from SharedPreferences
        _isLoggedIn.value = sharedPrefs.getBoolean("is_logged_in", false)

        allRecords = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Auth Actions
    fun login(usernameInput: String, passwordInput: String): Boolean {
        _loginError.value = null
        val trimmedUser = usernameInput.trim()
        val trimmedPass = passwordInput.trim()

        if (trimmedUser.isEmpty() || trimmedPass.isEmpty()) {
            _loginError.value = "Username dan Password tidak boleh kosong!"
            return false
        }

        // Standard default credential: admin / admin
        if (trimmedUser.lowercase() == "admin" && trimmedPass == "admin") {
            _isLoggedIn.value = true
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            return true
        } else {
            _loginError.value = "Username atau Password salah!"
            return false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
    }

    // Form Field Clear
    fun clearForm() {
        nama.value = ""
        tempatLahir.value = ""
        tanggalLahir.value = ""
        jenisKelamin.value = "Laki-laki"
        alamatLengkap.value = ""
        agama.value = "Islam"
        statusPerkawinan.value = "Belum Kawin"
        kewarganegaraan.value = "WNI"
        _editingRecordId.value = null
    }

    // Fill form for editing
    fun startEditing(record: DataRecord) {
        _editingRecordId.value = record.id
        nama.value = record.nama
        tempatLahir.value = record.tempatLahir
        tanggalLahir.value = record.tanggalLahir
        jenisKelamin.value = record.jenisKelamin
        alamatLengkap.value = record.alamatLengkap
        agama.value = record.agama
        statusPerkawinan.value = record.statusPerkawinan
        kewarganegaraan.value = record.kewarganegaraan
    }

    // Save record (insert or update)
    fun saveRecord(onSuccess: () -> Unit) {
        val nameVal = nama.value.trim()
        val placeVal = tempatLahir.value.trim()
        val dobVal = tanggalLahir.value.trim()
        val genderVal = jenisKelamin.value
        val addrVal = alamatLengkap.value.trim()
        val religionVal = agama.value
        val statusVal = statusPerkawinan.value
        val nationalityVal = kewarganegaraan.value

        if (nameVal.isEmpty() || placeVal.isEmpty() || dobVal.isEmpty() || addrVal.isEmpty()) {
            return // handled by UI validation
        }

        viewModelScope.launch(Dispatchers.IO) {
            val recordId = _editingRecordId.value
            val record = DataRecord(
                id = recordId ?: 0,
                nama = nameVal,
                tempatLahir = placeVal,
                tanggalLahir = dobVal,
                jenisKelamin = genderVal,
                alamatLengkap = addrVal,
                agama = religionVal,
                statusPerkawinan = statusVal,
                kewarganegaraan = nationalityVal,
                timestamp = System.currentTimeMillis()
            )

            if (recordId == null) {
                repository.insertRecord(record)
            } else {
                repository.updateRecord(record)
            }

            withContext(Dispatchers.Main) {
                clearForm()
                onSuccess()
            }
        }
    }

    // Delete record
    fun deleteRecord(record: DataRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(record)
        }
    }

    // Reset export status
    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }

    // Export to PDF
    fun exportPdf(context: Context) {
        _exportStatus.value = ExportStatus.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val records = allRecords.value
                if (records.isEmpty()) {
                    _exportStatus.value = ExportStatus.Error("Tidak ada data untuk diekspor!")
                    return@launch
                }
                val uri = ExportHelper.exportToPdf(context, records)
                if (uri != null) {
                    _exportStatus.value = ExportStatus.Success(uri, "PDF")
                } else {
                    _exportStatus.value = ExportStatus.Error("Gagal membuat file PDF!")
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    // Export to Excel (CSV)
    fun exportExcel(context: Context) {
        _exportStatus.value = ExportStatus.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val records = allRecords.value
                if (records.isEmpty()) {
                    _exportStatus.value = ExportStatus.Error("Tidak ada data untuk diekspor!")
                    return@launch
                }
                val uri = ExportHelper.exportToExcel(context, records)
                if (uri != null) {
                    _exportStatus.value = ExportStatus.Success(uri, "Excel (CSV)")
                } else {
                    _exportStatus.value = ExportStatus.Error("Gagal membuat file Excel!")
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}
