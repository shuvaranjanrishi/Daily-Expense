package com.therishideveloper.dailyexpense.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.data.entity.Transaction
import com.therishideveloper.dailyexpense.data.model.Category
import com.therishideveloper.dailyexpense.data.model.TransactionType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelHelper {

    private const val CHANNEL_ID = "download_channel"
    private const val AUTHORITY_SUFFIX = ".fileProvider"
    private const val FOLDER_NAME = "Daily Expense/Transaction Report"

    fun showDownloadNotification(context: Context, file: File) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_downloads),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val uri =
            FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(context.getString(R.string.notif_download_title))
            .setContentText(context.getString(R.string.notif_file_saved, file.name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // Notification permission revoked
        }
    }

    fun createTransactionExcel(context: Context, transactions: List<Transaction>): File? {
        return try {
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val root = File(downloadDir, FOLDER_NAME)
            if (!root.exists()) root.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val baseFileName = "Transaction_Report_$timeStamp"
            val file = getUniqueFile(root, baseFileName, "xlsx")

            XSSFWorkbook().use { workbook ->
                val sheet = workbook.createSheet(context.getString(R.string.excel_sheet_name))

                val headerRow = sheet.createRow(0)
                val headers = listOf(
                    context.getString(R.string.excel_col_date),
                    context.getString(R.string.excel_col_category),
                    context.getString(R.string.excel_col_description),
                    context.getString(R.string.excel_col_amount),
                    context.getString(R.string.excel_col_type)
                )
                headers.forEachIndexed { index, title ->
                    headerRow.createCell(index).setCellValue(title)
                }

                transactions.forEachIndexed { index, tx ->
                    val row = sheet.createRow(index + 1)
                    val category = Category.fromDbKey(tx.category)
                    val categoryLabel = context.getString(category.titleRes)
                    val type = TransactionType.fromDbKey(tx.transactionType)
                    val typeLabel = context.getString(type.titleRes)
                    val amount = NumberUtils.formatByLocale(context, tx.amount.toString())
                    row.createCell(0).setCellValue(DateUtils.formatToDisplay(context, tx.date))
                    row.createCell(1).setCellValue(categoryLabel)
                    row.createCell(2).setCellValue(tx.description)
                    row.createCell(3).setCellValue(amount)
                    row.createCell(4).setCellValue(typeLabel)
                }

                sheet.setColumnWidth(2, 30 * 256)

                FileOutputStream(file).use { out ->
                    workbook.write(out)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getUniqueFile(directory: File, baseName: String, extension: String): File {
        var file = File(directory, "$baseName.$extension")
        var counter = 1

        while (file.exists()) {
            file = File(directory, "${baseName}_($counter).$extension")
            counter++
        }
        return file
    }

    fun shareExcelFile(context: Context, file: File) {
        val uri =
            FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.excel_share_subject))
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.excel_share_body)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.excel_share_chooser))
            )
        } catch (_: ActivityNotFoundException) {
            // No share target available
        }
    }
}