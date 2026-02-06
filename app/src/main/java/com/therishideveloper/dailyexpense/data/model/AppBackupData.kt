package com.therishideveloper.dailyexpense.data.model

import com.therishideveloper.dailyexpense.data.entity.Note
import com.therishideveloper.dailyexpense.data.entity.Transaction

data class AppBackupData(
    val transactions: List<Transaction>,
    val notes: List<Note>
)