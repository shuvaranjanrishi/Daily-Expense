package com.therishideveloper.dailyexpense.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.component.MenuGridItem
import com.therishideveloper.dailyexpense.component.TransactionSummaryRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therishideveloper.dailyexpense.component.ActionButton
import com.therishideveloper.dailyexpense.component.CalculatorDialog
import com.therishideveloper.dailyexpense.component.CurrentBalance
import com.therishideveloper.dailyexpense.component.SolidPieChart
import com.therishideveloper.dailyexpense.component.SummaryClickableRow
import com.therishideveloper.dailyexpense.data.model.TransactionPeriod
import com.therishideveloper.dailyexpense.data.model.TransactionType
import com.therishideveloper.dailyexpense.navigation.Screens
import com.therishideveloper.dailyexpense.ui.theme.expenseRed
import com.therishideveloper.dailyexpense.ui.theme.tealColor
import com.therishideveloper.dailyexpense.util.DashboardUtils
import com.therishideveloper.dailyexpense.util.DateUtils
import com.therishideveloper.dailyexpense.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: TransactionViewModel
) {
    // --- States ---
    var showCalculator by remember { mutableStateOf(false) }
    var calcExpression by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("0") }

    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val incomeList by viewModel.allIncomeTransactions.collectAsStateWithLifecycle()
    val expenseList by viewModel.allExpenseTransactions.collectAsStateWithLifecycle()

    // --- Calculation Logic ---
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.amount } }
    val totalExpense = remember(expenseList) { expenseList.sumOf { it.amount } }
    val balance = totalIncome - totalExpense
    val incomeProgress = remember(totalIncome, totalExpense) {
        if (totalIncome + totalExpense > 0) (totalIncome / (totalIncome + totalExpense)).toFloat() else 0.5f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.home_title),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tealColor),
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu,
                            "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = { CurrentBalance(currentBalance.toString()) },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCalculator = true },
                containerColor = tealColor,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = "Calculator", modifier = Modifier.size(28.dp))
            }
        }

    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            val isNarrow = maxWidth < 360.dp
            val columns = when {
                maxWidth < 320.dp -> 2
                maxWidth >= 600.dp -> 4
                else -> 3
            }
            val pieSize = if (isNarrow) 82.dp else 100.dp
            val horizontalPad = if (isNarrow) 12.dp else 16.dp
            val dashboardMenus = remember { DashboardUtils.getDashboardMenus() }
            val menuRows = remember(dashboardMenus, columns) { dashboardMenus.chunked(columns) }

            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 88.dp)
            ) {
                DisplayTodayDate(horizontalPadding = horizontalPad)

                // --- Summary Card ---
                Card(
                    modifier = Modifier
                        .padding(horizontal = horizontalPad, vertical = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(if (isNarrow) 12.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(pieSize)) {
                            SolidPieChart(incomeProgress, tealColor, expenseRed)
                        }
                        Spacer(modifier = Modifier.width(if (isNarrow) 12.dp else 16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SummaryClickableRow(
                                stringResource(R.string.income),
                                totalIncome.toString(),
                                MaterialTheme.colorScheme.primary
                            ) {
                                navController.navigate("income_expense_screen/" + TransactionType.INCOME.dbKey)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            SummaryClickableRow(
                                stringResource(R.string.expense),
                                totalExpense.toString(),
                                MaterialTheme.colorScheme.error
                            ) {
                                navController.navigate("income_expense_screen/" + TransactionType.EXPENSE.dbKey)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            TransactionSummaryRow(
                                stringResource(R.string.balance),
                                balance.toString(),
                                if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // --- Adaptive Menu Grid ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPad - 6.dp)
                ) {
                    menuRows.forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->
                                MenuGridItem(
                                    item = item,
                                    modifier = Modifier.weight(1f)
                                ) { route ->
                                    handleNavigation(navController, route)
                                }
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // --- Primary Action Buttons ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPad, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        stringResource(R.string.add_income),
                        tealColor,
                        Modifier.weight(1f)
                    ) {
                        navController.navigate(Screens.AddIncomeScreen.route)
                    }
                    ActionButton(
                        stringResource(R.string.add_expense),
                        expenseRed,
                        Modifier.weight(1f)
                    ) {
                        navController.navigate(Screens.AddExpenseScreen.route)
                    }
                }
            }
        }

        if (showCalculator) {
            CalculatorDialog(
                initialExpression = calcExpression, initialResult = calcResult,
                onMinimize = { expr, res ->
                    calcExpression = expr; calcResult = res; showCalculator = false
                },
                onClose = { calcExpression = ""; calcResult = "0"; showCalculator = false }
            )
        }
    }
}

private fun handleNavigation(navController: NavController, route: String) {
    when (route) {
        // Handle period-based routes (today, monthly, yearly)
        TransactionPeriod.TODAY.dbKey,
        TransactionPeriod.MONTHLY.dbKey,
        TransactionPeriod.YEARLY.dbKey -> {
            navController.navigate("transaction_list/$route")
        }

        // Handle other static routes (notes, charts, etc.)
        else -> {
            try {
                navController.navigate(route)
            } catch (e: Exception) {
                // Prevent crash if route is missing
                Log.e("NavError", "Destination not found: $route")
            }
        }
    }
}

@Composable
private fun DisplayTodayDate(horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(end = horizontalPadding, start = horizontalPadding, top = 10.dp, bottom = 0.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = DateUtils.getTodayDateForHomeScreen(context),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
