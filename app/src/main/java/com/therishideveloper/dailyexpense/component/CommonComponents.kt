package com.therishideveloper.dailyexpense.component

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.ui.theme.expenseRed
import com.therishideveloper.dailyexpense.ui.theme.tealColor
import com.therishideveloper.dailyexpense.util.NumberUtils

@Composable
fun CurrentBalance(currentBalance: String) {
    val context = LocalContext.current
    val displayBalance = try {
        NumberUtils.formatAmountByLocale(context, currentBalance)
    } catch (e: Exception) {
        NumberUtils.formatByLocale(context, "0.00")
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Text(
            stringResource(R.string.label_current_balance),
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
        Text(
            displayBalance,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SolidPieChart(
    incomeProgress: Float,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val incomePercent = (incomeProgress * 100).toInt()
    val expensePercent = 100 - incomePercent

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = size.minDimension / 2
        val dynamicTextSize = (size.minDimension * 0.14f).coerceAtLeast(22f)

        // Draw Expense Slice
        drawArc(
            color = expenseColor.copy(alpha = 0.7f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true
        )

        // Draw Income Slice
        drawArc(
            color = incomeColor.copy(alpha = 0.7f),
            startAngle = -90f,
            sweepAngle = 360 * incomeProgress,
            useCenter = true
        )

        // Draw Percentage Text
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = dynamicTextSize
                isFakeBoldText = true
                typeface = Typeface.DEFAULT_BOLD
            }

            // Income Percentage Text
            val incomeMidAngle = (-90f + (360 * incomeProgress) / 2) * (Math.PI / 180)
            val incomeX = (canvasWidth / 2 + (radius / 1.55f) * Math.cos(incomeMidAngle)).toFloat()
            val incomeY = (canvasHeight / 2 + (radius / 1.55f) * Math.sin(incomeMidAngle)).toFloat()

            paint.color = Color.White.toArgb()
            if (incomePercent > 5) {
                drawText(
                    NumberUtils.formatByLocale(context, "$incomePercent%"),
                    incomeX,
                    incomeY + dynamicTextSize * 0.35f,
                    paint
                )
            }

            // Expense Percentage Text
            val expenseMidAngle =
                (-90f + 360 * incomeProgress + (360 * (1 - incomeProgress)) / 2) * (Math.PI / 180)
            val expenseX = (canvasWidth / 2 + (radius / 1.55f) * Math.cos(expenseMidAngle)).toFloat()
            val expenseY =
                (canvasHeight / 2 + (radius / 1.55f) * Math.sin(expenseMidAngle)).toFloat()

            paint.color = Color.White.toArgb()
            if (expensePercent > 5) {
                drawText(
                    NumberUtils.formatByLocale(context, "$expensePercent%"),
                    expenseX,
                    expenseY + dynamicTextSize * 0.35f,
                    paint
                )
            }
        }
    }
}

@Composable
fun TransactionSummaryRow(
    label: String,
    amount: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = NumberUtils.formatAmountByLocale(context, amount),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

@Composable
fun TransactionSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    incomeProgress: Float
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val pieSize = if (maxWidth < 360.dp) 80.dp else 100.dp

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(pieSize)) {
                    SolidPieChart(
                        incomeProgress = incomeProgress,
                        incomeColor = tealColor,
                        expenseColor = expenseRed
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TransactionSummaryRow(
                        stringResource(R.string.income),
                        totalIncome.toString(),
                        MaterialTheme.colorScheme.primary
                    )
                    TransactionSummaryRow(
                        stringResource(R.string.expense),
                        totalExpense.toString(),
                        MaterialTheme.colorScheme.error
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    TransactionSummaryRow(
                        stringResource(R.string.balance),
                        balance.toString(),
                        if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
