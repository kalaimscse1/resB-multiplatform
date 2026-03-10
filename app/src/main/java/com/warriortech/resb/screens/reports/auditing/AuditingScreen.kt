package com.warriortech.resb.screens.reports.auditing

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.warriortech.resb.model.TblAuditingResponse
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.AuditingReportViewModel
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditingScreen(
    drawerState: DrawerState,
    viewModel: AuditingReportViewModel = hiltViewModel()
) {
    val auditingState by viewModel.auditingState.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditing Report", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        androidx.compose.material.Icon(
                            Icons.Default.Menu, contentDescription = "Menu",
                            tint = SurfaceLight
                        )
                    }
                },
                actions = {
                    if (auditingState is AuditingReportViewModel.AuditingState.Success) {
                        val report = (auditingState as AuditingReportViewModel.AuditingState.Success).report
                        IconButton(onClick = { exportToExcel(context, report) }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Excel", tint = SurfaceLight)
                        }
                        IconButton(onClick = { exportToPdf(context, report) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = SurfaceLight)
                        }
                        IconButton(onClick = { shareReport(context, report) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = SurfaceLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            when (val state = auditingState) {
                is AuditingReportViewModel.AuditingState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is AuditingReportViewModel.AuditingState.Success -> {
                    AuditingTable(state.report)
                }
                is AuditingReportViewModel.AuditingState.Failure -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditingTable(report: List<TblAuditingResponse>) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(PrimaryGreen.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            TableHeader("Sl.No", 60.dp)
            TableHeader("ID", 60.dp)
            TableHeader("Modify Date", 100.dp)
            TableHeader("Modify Time", 100.dp)
            TableHeader("Groups", 120.dp)
            TableHeader("Counter", 120.dp)
            TableHeader("User", 120.dp)
            TableHeader("Created Date", 100.dp)
            TableHeader("Member", 120.dp)
            TableHeader("Member ID", 100.dp)
            TableHeader("Narration", 200.dp)
            TableHeader("Credit", 100.dp)
            TableHeader("Debit", 100.dp)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(report) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(item.slno.toString(), 60.dp)
                    TableCell(item.id.toString(), 60.dp)
                    TableCell(item.modify_date, 100.dp)
                    TableCell(item.modify_time, 100.dp)
                    TableCell(item.groups, 120.dp)
                    TableCell(item.counter.counter_name, 120.dp)
                    TableCell(item.user.user_name, 120.dp)
                    TableCell(item.created_date, 100.dp)
                    TableCell(item.member, 120.dp)
                    TableCell(item.member_id, 100.dp)
                    TableCell(item.narration, 200.dp)
                    TableCell(String.format("%.2f", item.credit), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format("%.2f", item.debit), 100.dp, textAlign = TextAlign.End)
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun TableHeader(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(4.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = PrimaryGreen
    )
}

@Composable
fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(4.dp),
        fontSize = 13.sp,
        textAlign = textAlign
    )
}

fun exportToExcel(context: Context, report: List<TblAuditingResponse>) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Auditing Report")
        val headerRow = sheet.createRow(0)
        val headers = listOf("SlNo", "ID", "Modify Date", "Modify Time", "Groups", "Counter", "User", "Created Date", "Member", "Member ID", "Narration", "Credit", "Debit")

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        report.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.slno.toDouble())
            row.createCell(1).setCellValue(item.id.toDouble())
            row.createCell(2).setCellValue(item.modify_date)
            row.createCell(3).setCellValue(item.modify_time)
            row.createCell(4).setCellValue(item.groups)
            row.createCell(5).setCellValue(item.counter.counter_name)
            row.createCell(6).setCellValue(item.user.user_name)
            row.createCell(7).setCellValue(item.created_date)
            row.createCell(8).setCellValue(item.member)
            row.createCell(9).setCellValue(item.member_id)
            row.createCell(10).setCellValue(item.narration)
            row.createCell(11).setCellValue(item.credit)
            row.createCell(12).setCellValue(item.debit)
        }

        val file = File(context.getExternalFilesDir(null), "AuditingReport.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        Toast.makeText(context, "Excel exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        openFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    } catch (e: Exception) {
        Toast.makeText(context, "Excel Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun exportToPdf(context: Context, report: List<TblAuditingResponse>) {
    try {
        val document = Document(PageSize.A4.rotate())
        val file = File(context.getExternalFilesDir(null), "AuditingReport.pdf")
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
        document.add(Paragraph("Auditing Report", titleFont).apply { alignment = Element.ALIGN_CENTER })
        document.add(Paragraph(" ")) // Spacer

        val table = PdfPTable(13)
        table.widthPercentage = 100f
        val headers = listOf("SlNo", "ID", "Date", "Time", "Groups", "Counter", "User", "Created", "Member", "Mem ID", "Narration", "Credit", "Debit")

        val headFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
        headers.forEach { header ->
            val cell = PdfPCell(Phrase(header, headFont))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = com.itextpdf.text.BaseColor.LIGHT_GRAY
            table.addCell(cell)
        }

        val cellFont = Font(Font.FontFamily.HELVETICA, 9f)
        report.forEach { item ->
            table.addCell(PdfPCell(Phrase(item.slno.toString(), cellFont)))
            table.addCell(PdfPCell(Phrase(item.id.toString(), cellFont)))
            table.addCell(PdfPCell(Phrase(item.modify_date, cellFont)))
            table.addCell(PdfPCell(Phrase(item.modify_time, cellFont)))
            table.addCell(PdfPCell(Phrase(item.groups, cellFont)))
            table.addCell(PdfPCell(Phrase(item.counter.counter_name, cellFont)))
            table.addCell(PdfPCell(Phrase(item.user.user_name, cellFont)))
            table.addCell(PdfPCell(Phrase(item.created_date, cellFont)))
            table.addCell(PdfPCell(Phrase(item.member, cellFont)))
            table.addCell(PdfPCell(Phrase(item.member_id, cellFont)))
            table.addCell(PdfPCell(Phrase(item.narration, cellFont)))
            table.addCell(PdfPCell(Phrase(item.credit.toString(), cellFont)))
            table.addCell(PdfPCell(Phrase(item.debit.toString(), cellFont)))
        }

        document.add(table)
        document.close()

        Toast.makeText(context, "PDF exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        openFile(context, file, "application/pdf")
    } catch (e: Exception) {
        Toast.makeText(context, "PDF Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareReport(context: Context, report: List<TblAuditingResponse>) {
    try {
        val file = File(context.getExternalFilesDir(null), "AuditingReport.xlsx")
        // Ensure file exists by exporting it first if needed, or just use the existing one
        exportToExcel(context, report)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Auditing Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share Failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}
