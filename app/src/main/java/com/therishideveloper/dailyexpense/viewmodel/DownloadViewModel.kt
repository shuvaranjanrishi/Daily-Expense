package com.therishideveloper.dailyexpense.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.dailyexpense.data.entity.Transaction
import com.therishideveloper.dailyexpense.util.ExcelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor() : ViewModel() {

    var isDownloading by mutableStateOf(false)
        private set

    var downloadedFile by mutableStateOf<File?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun downloadExcel(
        context: Context,
        transactions: List<Transaction>,
        onComplete: (File) -> Unit
    ) {
        resetState()
        if (transactions.isEmpty()) {
            errorMessage = "No transactions found to download!"
            return
        }

        viewModelScope.launch {
            isDownloading = true
            errorMessage = null
            try {
                val file = withContext(Dispatchers.IO) {
                    ExcelHelper.createTransactionExcel(context, transactions)
                }
                if (file != null) {
                    downloadedFile = file
                    onComplete(file)
                } else {
                    errorMessage = "Failed to create Excel file..."
                }
            } finally {
                isDownloading = false
            }
        }
    }

    fun shareExcel(transactions: List<Transaction>, context: Context) {
        resetState()
        if (transactions.isEmpty()) {
            errorMessage = "No transactions found to share!"
            return
        }

        viewModelScope.launch {
            isDownloading = true
            errorMessage = null
            try {
                val file = withContext(Dispatchers.IO) {
                    ExcelHelper.createTransactionExcel(context, transactions)
                }
                if (file != null) {
                    ExcelHelper.shareExcelFile(context, file)
                } else {
                    errorMessage = "Failed to create file for sharing..."
                }
            } finally {
                isDownloading = false
            }
        }
    }

    fun resetState() {
        downloadedFile = null
        errorMessage = null
    }
}