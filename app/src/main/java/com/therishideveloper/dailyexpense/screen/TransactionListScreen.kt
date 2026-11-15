package com.therishideveloper.dailyexpense.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.component.CalculatorDialog
import com.therishideveloper.dailyexpense.component.CurrentBalance
import com.therishideveloper.dailyexpense.component.TransactionItem
import com.therishideveloper.dailyexpense.component.TransactionSummaryCard
import com.therishideveloper.dailyexpense.data.model.TransactionPeriod
import com.therishideveloper.dailyexpense.data.model.TransactionType
import com.therishideveloper.dailyexpense.ui.theme.tealColor
import com.therishideveloper.dailyexpense.util.DateUtils
import com.therishideveloper.dailyexpense.util.NumberUtils
import com.therishideveloper.dailyexpense.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onBack: () -> Unit,
    transactionPeriod: String,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()

    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedMonth by remember {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var calcExpr by remember { mutableStateOf("") }
    var calcRes by remember { mutableStateOf("0") }

    val transactions by remember(
        transactionPeriod,
        date,
        selectedMonth,
        selectedYear
    ) {
        when (transactionPeriod) {
            TransactionPeriod.MONTHLY.dbKey -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth - 1)
                }
                viewModel.getMonthlyData(cal.timeInMillis)
            }

            TransactionPeriod.YEARLY.dbKey -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                }
                viewModel.getYearlyData(cal.timeInMillis)
            }

            else -> viewModel.getTodayData(date)
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val totalIncome = remember(transactions) {
        transactions.filter {
            TransactionType.fromDbKey(it.transactionType) == TransactionType.INCOME
        }.sumOf { it.amount }
    }

    val totalExpense = remember(transactions) {
        transactions.filter {
            TransactionType.fromDbKey(it.transactionType) == TransactionType.EXPENSE
        }.sumOf { it.amount }
    }

    val balance = totalIncome - totalExpense

    val incomeProgress = remember(totalIncome, totalExpense) {
        val total = totalIncome + totalExpense
        if (total > 0.0) (totalIncome / total).toFloat() else 0.5f
    }

    val allRecords = stringResource(R.string.label_all_records)
    val currentLocale = com.therishideveloper.dailyexpense.util.LocaleHelper.getSavedLocale(context)

    val displayText = remember(transactionPeriod, date, selectedMonth, selectedYear, currentLocale) {
        when (transactionPeriod) {
            TransactionPeriod.MONTHLY.dbKey -> {
                DateUtils.getLocalizedMonthYear(context, selectedMonth, selectedYear)
            }

            TransactionPeriod.YEARLY.dbKey -> {
                NumberUtils.formatByLocale(context, selectedYear.toString())
            }

            TransactionPeriod.TODAY.dbKey -> {
                DateUtils.formatToDisplay(context, date)
            }

            else -> allRecords
        }
    }

    val isFabVisible by remember(transactions) {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo

            if (visibleItems.isEmpty()) {
                true
            } else {
                val lastItem = visibleItems.lastOrNull()
                val totalItemsCount = layoutInfo.totalItemsCount

                if (lastItem != null && lastItem.index == totalItemsCount - 1) {
                    val viewportEnd = layoutInfo.viewportEndOffset
                    val fabTopBoundary = viewportEnd - 250 // বাটনের উপরের সীমানা
                    val fabBottomBoundary = viewportEnd - 50 // বাটনের নিচের সীমানা
                    val itemTop = lastItem.offset
                    val itemBottom = lastItem.offset + lastItem.size
                    val isOverlapping = itemBottom > fabTopBoundary && itemTop < fabBottomBoundary

                    !isOverlapping
                } else {
                    true
                }
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            type = transactionPeriod,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onDismiss = { showMonthYearPicker = false },
            onSelection = { month, year ->
                selectedMonth = month
                selectedYear = year
                showMonthYearPicker = false
            }
        )
    }

    if (showCalculator) {
        CalculatorDialog(
            initialExpression = calcExpr, initialResult = calcRes,
            onMinimize = { e, r -> calcExpr = e; calcRes = r; showCalculator = false },
            onClose = { calcExpr = ""; calcRes = "0"; showCalculator = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        DateUtils.getScreenTitle(transactionPeriod),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tealColor),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = { CurrentBalance(currentBalance?.toString() ?: "0.0") }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { showCalculator = true },
                    containerColor = tealColor,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Calculate, null, modifier = Modifier.size(30.dp))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                DateFilterHeader(
                    text = displayText,
                    onClick = {
                        if (transactionPeriod == TransactionPeriod.TODAY.dbKey) {
                            DateUtils.showDatePicker(context, date) {
                                date = it
                            }
                        } else showMonthYearPicker = true
                    }
                )

                TransactionSummaryCard(totalIncome, totalExpense, balance, incomeProgress)

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(transactions, key = { it.id }) { item ->
                        TransactionItem(transaction = item)
                    }
                }
            }
        }
    }
}

@Composable
fun DateFilterHeader(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthYearPickerDialog(
    type: String,
    selectedMonth: Int,
    selectedYear: Int,
    onDismiss: () -> Unit,
    onSelection: (month: Int, year: Int) -> Unit
) {
    val context = LocalContext.current
    var tempMonth by remember { mutableIntStateOf(selectedMonth) }
    var tempYear by remember { mutableIntStateOf(selectedYear) }
    val isMonthly = type == TransactionPeriod.MONTHLY.dbKey

    // বছরগুলোর লিস্ট
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val startYear = 2020
    val years = remember { (startYear..currentYear + 5).toList() }

    val listState = rememberLazyListState()

    LaunchedEffect(key1 = selectedYear) {
        val index = years.indexOf(selectedYear)
        if (index != -1) {
            listState.scrollToItem(index = index, scrollOffset = -200)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (isMonthly) R.string.title_select_month_year else R.string.title_select_year))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.label_year), fontWeight = FontWeight.Bold)

                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(years) { year ->
                        FilterChip(
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tealColor,
                                selectedLabelColor = Color.White,
                            ),
                            selected = tempYear == year,
                            onClick = { tempYear = year },
                            label = { Text(NumberUtils.formatByLocale(context, year.toString())) }
                        )
                    }
                }

                if (isMonthly) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.label_month), fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0..11).forEach { index ->
                            FilterChip(
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tealColor,
                                    selectedLabelColor = Color.White,
                                ),
                                selected = tempMonth == index + 1,
                                onClick = { tempMonth = index + 1 },
                                label = {
                                    Text(DateUtils.getLocalizedMonthName(context, index))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelection(tempMonth, tempYear) }) {
                Text(
                    stringResource(R.string.action_apply),
                    color = tealColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

