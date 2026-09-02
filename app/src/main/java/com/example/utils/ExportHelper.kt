package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.model.DataRecord
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    /**
     * Helper to save files to Downloads folder.
     * Uses MediaStore for API 29+ (Scoped Storage, zero-permission),
     * and falls back to Environment.getExternalStoragePublicDirectory for API < 29.
     */
    private fun saveFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        writeBlock: (OutputStream) -> Unit
    ): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Places in Downloads/FormulirData
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FormulirData")
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        writeBlock(outputStream)
                    }
                    uri
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        } else {
            // Legacy Storage API fallback (API < 29)
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloadDir, "FormulirData")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val targetFile = File(appDir, fileName)
            try {
                FileOutputStream(targetFile).use { fos ->
                    writeBlock(fos)
                }
                Uri.fromFile(targetFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Exports records to Excel-compatible CSV file.
     * Uses Semicolon separator and UTF-8 BOM for maximum compatibility with Microsoft Excel.
     */
    fun exportToExcel(context: Context, records: List<DataRecord>): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Data_Isian_$timestamp.csv"
        val mimeType = "text/csv"

        return saveFileToDownloads(context, fileName, mimeType) { outputStream ->
            // Write UTF-8 Byte Order Mark (BOM) so Excel opens it with UTF-8 instantly
            outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                // Columns header
                val headers = listOf(
                    "No", "Nama", "Tempat Lahir", "Tanggal Lahir", 
                    "Jenis Kelamin", "Alamat Lengkap", "Agama", 
                    "Status Perkawinan", "Kewarganegaraan"
                )
                writer.write(headers.joinToString(";") { "\"$it\"" })
                writer.newLine()

                // Rows
                records.forEachIndexed { index, record ->
                    val row = listOf(
                        (index + 1).toString(),
                        record.nama,
                        record.tempatLahir,
                        record.tanggalLahir,
                        record.jenisKelamin,
                        record.alamatLengkap,
                        record.agama,
                        record.statusPerkawinan,
                        record.kewarganegaraan
                    )
                    writer.write(row.joinToString(";") { "\"${it.replace("\"", "\"\"")}\"" })
                    writer.newLine()
                }
            }
        }
    }

    /**
     * Exports records to PDF.
     * Table columns aligned precisely with:
     * - "Alamat Lengkap" column made extra wide to support long text.
     * - Header columns centered.
     * - Under title, "dicetak pada tanggal" text is shown and underlined.
     */
    fun exportToPdf(context: Context, records: List<DataRecord>): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Data_Isian_$timestamp.pdf"
        val mimeType = "application/pdf"

        return saveFileToDownloads(context, fileName, mimeType) { outputStream ->
            val pdfDocument = PdfDocument()

            // Page dimensions (A4 size: 595 x 842 points)
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            // Setup paints
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val metaPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 8.5f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }

            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val cellPaint = Paint().apply {
                color = Color.BLACK
                textSize = 7.5f
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
            }

            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#F2F2F2")
                style = Paint.Style.FILL
            }

            // Margins
            val leftMargin = 20f
            val rightMargin = pageWidth - 20f
            val tableWidth = rightMargin - leftMargin // 555f

            // Define column widths: (Sum = 555)
            // No = 20, Nama = 65, Tempat Tgl Lahir = 80, JK = 35, Alamat = 160 (EXTRA WIDE!), Agama = 55, Status = 70, Kewarganegaraan = 70
            val colNoWidth = 20f
            val colNamaWidth = 65f
            val colTtlWidth = 80f
            val colJkWidth = 35f
            val colAlamatWidth = 160f // Made extra wide!
            val colAgamaWidth = 55f
            val colStatusWidth = 70f
            val colKwnWidth = 70f

            val colXCoords = floatArrayOf(
                leftMargin, // No
                leftMargin + colNoWidth, // Nama
                leftMargin + colNoWidth + colNamaWidth, // TTL
                leftMargin + colNoWidth + colNamaWidth + colTtlWidth, // JK
                leftMargin + colNoWidth + colNamaWidth + colTtlWidth + colJkWidth, // Alamat
                leftMargin + colNoWidth + colNamaWidth + colTtlWidth + colJkWidth + colAlamatWidth, // Agama
                leftMargin + colNoWidth + colNamaWidth + colTtlWidth + colJkWidth + colAlamatWidth + colAgamaWidth, // Status
                leftMargin + colNoWidth + colNamaWidth + colTtlWidth + colJkWidth + colAlamatWidth + colAgamaWidth + colStatusWidth // Kwn
            )

            val headers = listOf(
                "No", "Nama", "Tempat, Tgl Lahir", "JK", 
                "Alamat Lengkap", "Agama", "Status", "Kwn"
            )

            // Current date formatted beautifully
            val printDateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
            val printedOnText = "dicetak pada tanggal: $printDateStr"

            // Pagination setup
            var currentY = 110f
            val bottomLimit = pageHeight - 40f

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Function to draw header on current page
            fun drawPageHeader(canvas: Canvas) {
                // Header Title
                canvas.drawText("LAPORAN DATA ISIAN FORMULIR", (pageWidth / 2).toFloat(), 40f, titlePaint)
                
                // Subtitle "dicetak pada tanggal..."
                canvas.drawText(printedOnText, leftMargin, 58f, metaPaint)
                
                // Horizontal line below title and printed date
                canvas.drawLine(leftMargin, 68f, rightMargin, 68f, linePaint)

                // Draw Table Header
                val headerHeight = 22f
                val headerY = 80f
                
                // Header Background
                canvas.drawRect(leftMargin, headerY, rightMargin, headerY + headerHeight, headerBgPaint)
                canvas.drawRect(leftMargin, headerY, rightMargin, headerY + headerHeight, borderPaint)

                // Header Texts - Centered as requested
                for (i in headers.indices) {
                    val colStart = colXCoords[i]
                    val colWidth = when (i) {
                        0 -> colNoWidth
                        1 -> colNamaWidth
                        2 -> colTtlWidth
                        3 -> colJkWidth
                        4 -> colAlamatWidth
                        5 -> colAgamaWidth
                        6 -> colStatusWidth
                        else -> colKwnWidth
                    }

                    // For centering, we set Paint Alignment to CENTER and place text at colStart + colWidth/2
                    val textPaintCenter = TextPaint(headerPaint).apply {
                        textAlign = Paint.Align.CENTER
                    }

                    val textX = colStart + (colWidth / 2)
                    val textY = headerY + 14f // Center vertical padding roughly

                    canvas.drawText(headers[i], textX, textY, textPaintCenter)

                    // Draw column vertical boundary
                    if (i > 0) {
                        canvas.drawLine(colStart, headerY, colStart, headerY + headerHeight, borderPaint)
                    }
                }
            }

            // Draw header on the first page
            drawPageHeader(canvas)
            currentY = 102f // Table body starts right after header

            // Loop and draw all records with pagination
            records.forEachIndexed { index, record ->
                val ttlCombined = "${record.tempatLahir}, ${record.tanggalLahir}"
                
                // Create StaticLayouts for cells to measure their heights and wrap if too long
                val textPaint = TextPaint(cellPaint)
                
                val slNo = createStaticLayout((index + 1).toString(), textPaint, colNoWidth.toInt() - 4)
                val slNama = createStaticLayout(record.nama, textPaint, colNamaWidth.toInt() - 6)
                val slTtl = createStaticLayout(ttlCombined, textPaint, colTtlWidth.toInt() - 6)
                val slJk = createStaticLayout(record.jenisKelamin, textPaint, colJkWidth.toInt() - 4)
                val slAlamat = createStaticLayout(record.alamatLengkap, textPaint, colAlamatWidth.toInt() - 8)
                val slAgama = createStaticLayout(record.agama, textPaint, colAgamaWidth.toInt() - 6)
                val slStatus = createStaticLayout(record.statusPerkawinan, textPaint, colStatusWidth.toInt() - 6)
                val slKwn = createStaticLayout(record.kewarganegaraan, textPaint, colKwnWidth.toInt() - 6)

                // Determine row height based on tallest column cell
                val maxCellHeight = listOf(
                    slNo.height, slNama.height, slTtl.height, slJk.height, 
                    slAlamat.height, slAgama.height, slStatus.height, slKwn.height
                ).maxOrNull() ?: 15

                val rowHeight = maxCellHeight + 10f // add padding

                // Check if row exceeds bottom margin of page
                if (currentY + rowHeight > bottomLimit) {
                    // Draw outer border and column borders for the current page before finishing
                    canvas.drawRect(leftMargin, 102f, rightMargin, currentY, borderPaint)
                    for (i in 1 until colXCoords.size) {
                        canvas.drawLine(colXCoords[i], 102f, colXCoords[i], currentY, borderPaint)
                    }

                    // Finish page
                    pdfDocument.finishPage(page)
                    
                    // Create new page
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    
                    // Draw headers on new page
                    drawPageHeader(canvas)
                    currentY = 102f
                }

                // Draw row separator line at the bottom of this row
                canvas.drawLine(leftMargin, currentY + rowHeight, rightMargin, currentY + rowHeight, borderPaint)

                // Draw cell contents using StaticLayout.draw
                val layouts = listOf(slNo, slNama, slTtl, slJk, slAlamat, slAgama, slStatus, slKwn)
                for (i in layouts.indices) {
                    val colStart = colXCoords[i]
                    val colWidth = when (i) {
                        0 -> colNoWidth
                        1 -> colNamaWidth
                        2 -> colTtlWidth
                        3 -> colJkWidth
                        4 -> colAlamatWidth
                        5 -> colAgamaWidth
                        6 -> colStatusWidth
                        else -> colKwnWidth
                    }

                    val layout = layouts[i]
                    
                    // Vertical centering of text in cell
                    val paddingY = (rowHeight - layout.height) / 2
                    val startX = colStart + when(i) {
                        0, 3 -> (colWidth - layout.width) / 2 // Center "No" and "Jenis Kelamin"
                        else -> 4f // Margins for other text fields
                    }

                    canvas.save()
                    canvas.translate(startX, currentY + paddingY)
                    layout.draw(canvas)
                    canvas.restore()
                }

                currentY += rowHeight
            }

            // Draw outer border and vertical column borders for the final page
            canvas.drawRect(leftMargin, 102f, rightMargin, currentY, borderPaint)
            for (i in 1 until colXCoords.size) {
                canvas.drawLine(colXCoords[i], 102f, colXCoords[i], currentY, borderPaint)
            }

            // Finish the final page
            pdfDocument.finishPage(page)

            // Write document out
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
        }
    }

    private fun createStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        val boundedWidth = if (width < 5) 5 else width
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, boundedWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()
    }
}
