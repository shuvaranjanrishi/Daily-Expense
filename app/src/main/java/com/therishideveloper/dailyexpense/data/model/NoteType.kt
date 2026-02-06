package com.therishideveloper.dailyexpense.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.ui.theme.expenseRed
import com.therishideveloper.dailyexpense.ui.theme.tealColor

enum class NoteType(
    val dbKey: String,
    val titleRes: Int,
    val color: Color,
    val icon: ImageVector
) {
    DEBT(
        dbKey = "DEBT",
        titleRes = R.string.filter_debt,
        color = expenseRed,
        icon = Icons.AutoMirrored.Filled.CallMade
    ),

    RECEIVABLE(
        dbKey = "RECEIVABLE",
        titleRes = R.string.filter_receivable,
        color = tealColor,
        icon = Icons.AutoMirrored.Filled.CallReceived // Arrow pointing in (Money coming in)
    );

    companion object {

        fun fromDbKey(key: String?): NoteType {
            return entries.find { it.dbKey == key } ?: DEBT
        }

        fun getFilterList() = entries.toList()
    }
}