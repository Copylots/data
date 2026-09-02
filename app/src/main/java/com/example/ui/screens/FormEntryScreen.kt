package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FormViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEntryScreen(
    viewModel: FormViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form states from ViewModel
    val nama by viewModel.nama.collectAsState()
    val tempatLahir by viewModel.tempatLahir.collectAsState()
    val tanggalLahir by viewModel.tanggalLahir.collectAsState()
    val jenisKelamin by viewModel.jenisKelamin.collectAsState()
    val alamatLengkap by viewModel.alamatLengkap.collectAsState()
    val agama by viewModel.agama.collectAsState()
    val statusPerkawinan by viewModel.statusPerkawinan.collectAsState()
    val kewarganegaraan by viewModel.kewarganegaraan.collectAsState()
    val editingRecordId by viewModel.editingRecordId.collectAsState()

    // Error states
    var nameError by remember { mutableStateOf(false) }
    var tempatLahirError by remember { mutableStateOf(false) }
    var tanggalLahirError by remember { mutableStateOf(false) }
    var alamatError by remember { mutableStateOf(false) }

    // Dropdown Expanded States
    var isAgamaExpanded by remember { mutableStateOf(false) }
    var isStatusExpanded by remember { mutableStateOf(false) }

    // Options Lists
    val agamaOptions = listOf("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Khonghucu", "Lainnya")
    val statusOptions = listOf("Belum Kawin", "Kawin", "Cerai Hidup", "Cerai Mati")

    // Date Picker Setup
    val calendar = Calendar.getInstance()
    // Default to a reasonable birth date year for DOB inputs (e.g., 2000)
    if (tanggalLahir.isEmpty()) {
        calendar.set(Calendar.YEAR, 2000)
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
    } else {
        try {
            val parts = tanggalLahir.split("-")
            if (parts.size == 3) {
                calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                calendar.set(Calendar.YEAR, parts[2].toInt())
            }
        } catch (_: Exception) {}
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            viewModel.tanggalLahir.value = formattedDate
            tanggalLahirError = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingRecordId == null) "Tambah Isian Data" else "Edit Isian Data",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Information Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Silakan isi lengkap seluruh kolom di bawah ini dengan data yang valid.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // --- Form Inputs ---

            // 1. Nama Lengkap
            OutlinedTextField(
                value = nama,
                onValueChange = {
                    viewModel.nama.value = it
                    if (it.trim().isNotEmpty()) nameError = false
                },
                label = { Text("Nama Lengkap *") },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Nama Lengkap wajib diisi!", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_nama")
            )

            // 2. Tempat Lahir
            OutlinedTextField(
                value = tempatLahir,
                onValueChange = {
                    viewModel.tempatLahir.value = it
                    if (it.trim().isNotEmpty()) tempatLahirError = false
                },
                label = { Text("Tempat Lahir *") },
                isError = tempatLahirError,
                supportingText = {
                    if (tempatLahirError) {
                        Text("Tempat Lahir wajib diisi!", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_tempat_lahir")
            )

            // 3. Tanggal Lahir
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus()
                        datePickerDialog.show()
                    }
            ) {
                OutlinedTextField(
                    value = tanggalLahir,
                    onValueChange = { },
                    label = { Text("Tanggal Lahir *") },
                    isError = tanggalLahirError,
                    supportingText = {
                        if (tanggalLahirError) {
                            Text("Tanggal Lahir wajib diisi!", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Format: DD-MM-YYYY (Ketuk untuk memilih)")
                        }
                    },
                    readOnly = true,
                    enabled = false, // makes it non-focusable by keyboard but styling still works
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (tanggalLahirError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                        disabledSupportingTextColor = if (tanggalLahirError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tanggal_lahir")
                )
            }

            // 4. Jenis Kelamin (Radio Buttons inside a Card for visual grouping)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = ExposedDropdownMenuDefaults.outlinedTextFieldColors().run {
                    CardDefaults.outlinedCardBorder(true)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Jenis Kelamin *",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.jenisKelamin.value = "Laki-laki" }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = jenisKelamin == "Laki-laki",
                                onClick = { viewModel.jenisKelamin.value = "Laki-laki" },
                                modifier = Modifier.testTag("radio_jk_l")
                            )
                            Text("Laki-laki", fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.jenisKelamin.value = "Perempuan" }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = jenisKelamin == "Perempuan",
                                onClick = { viewModel.jenisKelamin.value = "Perempuan" },
                                modifier = Modifier.testTag("radio_jk_p")
                            )
                            Text("Perempuan", fontSize = 15.sp)
                        }
                    }
                }
            }

            // 5. Alamat Lengkap (Large Text Area)
            OutlinedTextField(
                value = alamatLengkap,
                onValueChange = {
                    viewModel.alamatLengkap.value = it
                    if (it.trim().isNotEmpty()) alamatError = false
                },
                label = { Text("Alamat Lengkap *") },
                isError = alamatError,
                supportingText = {
                    if (alamatError) {
                        Text("Alamat Lengkap wajib diisi!", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Masukkan alamat lengkap RT/RW, Kecamatan, Kota/Kabupaten")
                    }
                },
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_alamat")
            )

            // 6. Agama (Dropdown Selection)
            ExposedDropdownMenuBox(
                expanded = isAgamaExpanded,
                onExpandedChange = { isAgamaExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = agama,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Agama *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAgamaExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("input_agama_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = isAgamaExpanded,
                    onDismissRequest = { isAgamaExpanded = false }
                ) {
                    agamaOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.agama.value = option
                                isAgamaExpanded = false
                            }
                        )
                    }
                }
            }

            // 7. Status Perkawinan (Dropdown Selection)
            ExposedDropdownMenuBox(
                expanded = isStatusExpanded,
                onExpandedChange = { isStatusExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = statusPerkawinan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Perkawinan *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("input_status_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = isStatusExpanded,
                    onDismissRequest = { isStatusExpanded = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.statusPerkawinan.value = option
                                isStatusExpanded = false
                            }
                        )
                    }
                }
            }

            // 8. Kewarganegaraan
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = ExposedDropdownMenuDefaults.outlinedTextFieldColors().run {
                    CardDefaults.outlinedCardBorder(true)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kewarganegaraan *",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.kewarganegaraan.value = "WNI" }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = kewarganegaraan == "WNI",
                                onClick = { viewModel.kewarganegaraan.value = "WNI" },
                                modifier = Modifier.testTag("radio_kwn_wni")
                            )
                            Text("Warga Negara Indonesia (WNI)", fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.kewarganegaraan.value = "WNA" }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = kewarganegaraan == "WNA",
                                onClick = { viewModel.kewarganegaraan.value = "WNA" },
                                modifier = Modifier.testTag("radio_kwn_wna")
                            )
                            Text("WNA", fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (Save & Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearForm()
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // Validate
                        var isValid = true
                        if (nama.trim().isEmpty()) {
                            nameError = true
                            isValid = false
                        }
                        if (tempatLahir.trim().isEmpty()) {
                            tempatLahirError = true
                            isValid = false
                        }
                        if (tanggalLahir.trim().isEmpty()) {
                            tanggalLahirError = true
                            isValid = false
                        }
                        if (alamatLengkap.trim().isEmpty()) {
                            alamatError = true
                            isValid = false
                        }

                        if (isValid) {
                            viewModel.saveRecord {
                                onBack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
