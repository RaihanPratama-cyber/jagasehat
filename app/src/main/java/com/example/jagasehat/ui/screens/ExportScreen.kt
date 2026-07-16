package com.example.jagasehat.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jagasehat.model.FamilyMember
import com.example.jagasehat.model.HealthRecord
import com.example.jagasehat.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val allRecords by viewModel.getAllHealthRecords().collectAsState(initial = emptyList())
    val timestamp = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Export Data Komunitas", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
        Text("Unduh rekap data kesehatan seluruh komunitas yang telah masuk ke dalam sistem.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        Card(
            onClick = { exportToPdfPremium(context, allRecords, state.familyMembers, timestamp) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEE2E2), modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Article, contentDescription = "PDF", tint = Color(0xFFEF4444), modifier = Modifier.padding(12.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text("Download sebagai PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray); Text("Format dokumen resmi & rapi", fontSize = 12.sp, color = Color.Gray) }
            }
        }

        Card(
            onClick = { exportToTxtPremium(context, allRecords, state.familyMembers, timestamp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE0F2FE), modifier = Modifier.size(48.dp)) { Icon(Icons.Default.List, contentDescription = "TXT", tint = Color(0xFF0EA5E9), modifier = Modifier.padding(12.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text("Download sebagai Text (.txt)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray); Text("Format data terstruktur", fontSize = 12.sp, color = Color.Gray) }
            }
        }
    }
}

private data class ExportSummary(
    val sys: Int,
    val dia: Int,
    val bpm: Int,
    val sugar: Int,
    val weight: Int
)

private fun buildSummary(records: List<HealthRecord>): ExportSummary {
    fun List<Int>.avgInt() = if (isEmpty()) 0 else average().toInt()
    return ExportSummary(
        sys = records.mapNotNull { it.bloodPressureSystolic }.avgInt(),
        dia = records.mapNotNull { it.bloodPressureDiastolic }.avgInt(),
        bpm = records.mapNotNull { it.heartRate }.avgInt(),
        sugar = records.mapNotNull { it.bloodSugar?.toInt() }.avgInt(),
        weight = records.mapNotNull { it.weight?.toInt() }.avgInt()
    )
}

private fun memberName(memberId: String, members: List<FamilyMember>): String {
    return members.find { it.id == memberId }?.name ?: "-"
}

private fun exportToPdfPremium(context: Context, records: List<HealthRecord>, members: List<FamilyMember>, timestamp: String) {
    try {
        val document = PdfDocument()
        val summary = buildSummary(records)
        val sortedRecords = records.sortedByDescending { it.createdAt }
        val titlePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 21f; color = android.graphics.Color.parseColor("#10B981") }
        val subtitlePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 11f; color = android.graphics.Color.parseColor("#64748B") }
        val headerPaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f; color = android.graphics.Color.parseColor("#1E293B") }
        val labelPaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 11f; color = android.graphics.Color.parseColor("#475569") }
        val valuePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 11f; color = android.graphics.Color.parseColor("#0F172A") }
        val smallPaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f; color = android.graphics.Color.parseColor("#334155") }
        val linePaint = Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0"); strokeWidth = 1.5f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        var y = drawPdfHeader(canvas, titlePaint, subtitlePaint, linePaint, timestamp, pageNumber)

        canvas.drawText("Ringkasan Rata-Rata Data", 50f, y, headerPaint)
        y += 30f
        canvas.drawText("Tekanan Darah", 50f, y, labelPaint); canvas.drawText(": ${summary.sys}/${summary.dia} mmHg", 180f, y, valuePaint)
        canvas.drawText("Detak Jantung", 330f, y, labelPaint); canvas.drawText(": ${summary.bpm} BPM", 445f, y, valuePaint)
        y += 22f
        canvas.drawText("Gula Darah", 50f, y, labelPaint); canvas.drawText(": ${summary.sugar} mg/dL", 180f, y, valuePaint)
        canvas.drawText("Berat Badan", 330f, y, labelPaint); canvas.drawText(": ${summary.weight} Kg", 445f, y, valuePaint)
        y += 35f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 28f
        canvas.drawText("Rekap Sejarah Pemeriksaan", 50f, y, headerPaint)
        y += 22f
        drawTableHeader(canvas, valuePaint, linePaint, y)
        y += 22f

        if (sortedRecords.isEmpty()) {
            canvas.drawText("Belum ada data pemeriksaan yang tersimpan.", 50f, y, labelPaint)
        } else {
            sortedRecords.forEachIndexed { index, record ->
                if (y > 790f) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                    canvas = page.canvas
                    y = drawPdfHeader(canvas, titlePaint, subtitlePaint, linePaint, timestamp, pageNumber)
                    drawTableHeader(canvas, valuePaint, linePaint, y)
                    y += 22f
                }
                val td = if (record.bloodPressureSystolic != null && record.bloodPressureDiastolic != null) "${record.bloodPressureSystolic}/${record.bloodPressureDiastolic}" else "-"
                val bpm = record.heartRate?.toString() ?: "-"
                val sugar = record.bloodSugar?.toInt()?.toString() ?: "-"
                val weight = record.weight?.toInt()?.toString() ?: "-"
                val note = record.notes?.takeIf { it.isNotBlank() } ?: "-"
                canvas.drawText((index + 1).toString(), 50f, y, smallPaint)
                canvas.drawText(record.date.take(17), 72f, y, smallPaint)
                canvas.drawText(ellipsize(memberName(record.memberId, members), 13), 173f, y, smallPaint)
                canvas.drawText(td, 255f, y, smallPaint)
                canvas.drawText(bpm, 315f, y, smallPaint)
                canvas.drawText(sugar, 365f, y, smallPaint)
                canvas.drawText(weight, 422f, y, smallPaint)
                canvas.drawText(ellipsize(note, 18), 477f, y, smallPaint)
                y += 18f
            }
        }

        document.finishPage(page)
        val filename = "Laporan_JagaSehat_${System.currentTimeMillis()}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/JagaSehat")
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
            Toast.makeText(context, "Laporan PDF berhasil disimpan", Toast.LENGTH_LONG).show()
        }
        document.close()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun drawPdfHeader(
    canvas: android.graphics.Canvas,
    titlePaint: Paint,
    subtitlePaint: Paint,
    linePaint: Paint,
    timestamp: String,
    pageNumber: Int
): Float {
    var y = 70f
    canvas.drawText("REKAPITULASI KESEHATAN KOMUNITAS", 50f, y, titlePaint)
    y += 22f
    canvas.drawText("Aplikasi JagaSehat", 50f, y, subtitlePaint)
    canvas.drawText("Tanggal Cetak: $timestamp", 50f, y + 17f, subtitlePaint)
    canvas.drawText("Halaman $pageNumber", 475f, y + 17f, subtitlePaint)
    y += 42f
    canvas.drawLine(50f, y, 545f, y, linePaint)
    return y + 30f
}

private fun drawTableHeader(canvas: android.graphics.Canvas, paint: Paint, linePaint: Paint, y: Float) {
    canvas.drawText("No", 50f, y, paint)
    canvas.drawText("Tanggal", 72f, y, paint)
    canvas.drawText("Nama", 173f, y, paint)
    canvas.drawText("TD", 255f, y, paint)
    canvas.drawText("BPM", 315f, y, paint)
    canvas.drawText("Gula", 365f, y, paint)
    canvas.drawText("Berat", 422f, y, paint)
    canvas.drawText("Catatan", 477f, y, paint)
    canvas.drawLine(50f, y + 8f, 545f, y + 8f, linePaint)
}

private fun ellipsize(text: String, max: Int): String {
    return if (text.length <= max) text else text.take(max - 3) + "..."
}

private fun exportToTxtPremium(context: Context, records: List<HealthRecord>, members: List<FamilyMember>, timestamp: String) {
    try {
        val summary = buildSummary(records)
        val detail = records.sortedByDescending { it.createdAt }.joinToString("\n") { record ->
            val td = if (record.bloodPressureSystolic != null && record.bloodPressureDiastolic != null) "${record.bloodPressureSystolic}/${record.bloodPressureDiastolic} mmHg" else "-"
            val bpm = record.heartRate?.let { "$it BPM" } ?: "-"
            val sugar = record.bloodSugar?.let { "${it.toInt()} mg/dL" } ?: "-"
            val weight = record.weight?.let { "${it.toInt()} Kg" } ?: "-"
            val note = record.notes?.takeIf { it.isNotBlank() } ?: "-"
            "Tanggal: ${record.date}\nNama: ${memberName(record.memberId, members)}\nTekanan Darah: $td\nDetak Jantung: $bpm\nGula Darah: $sugar\nBerat Badan: $weight\nCatatan: $note\n------------------------------------------------------------"
        }.ifBlank { "Belum ada data pemeriksaan.\n------------------------------------------------------------" }

        val dataStr = """
============================================================
           LAPORAN KESEHATAN KOMUNITAS JAGASEHAT
============================================================
Tanggal Cetak : $timestamp
Sumber Data   : Aplikasi JagaSehat
------------------------------------------------------------

[RINGKASAN RATA-RATA KESEHATAN]
Tekanan Darah : ${summary.sys}/${summary.dia} mmHg
Detak Jantung : ${summary.bpm} BPM
Gula Darah    : ${summary.sugar} mg/dL
Berat Badan   : ${summary.weight} Kg

[REKAP SEJARAH PEMERIKSAAN]
$detail
============================================================
        """.trimIndent()

        val filename = "Data_JagaSehat_${System.currentTimeMillis()}.txt"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/JagaSehat")
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(dataStr.toByteArray()) }
            Toast.makeText(context, "Data TXT berhasil disimpan", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekspor TXT: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
