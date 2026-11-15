package com.therishideveloper.dailyexpense.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Share
import com.therishideveloper.dailyexpense.data.model.NavItem

enum class Screens(val route: String) {
    HomeScreen("home"),
    LanguageScreen("language"),
    BackupScreen("backup"),
    TransactionListScreen("transaction_list/{type}"),
    IncomeExpenseScreen("income_expense_screen/{type}"),
    NoteScreen("note"),
    AllTransactionsScreen("all_transactions"),
    CategoryChartScreen("category_chart"),
    AddIncomeScreen("add_income_screen"),
    AddExpenseScreen("add_expense_screen"),
    ShareAppScreen("share_app"),
    AboutScreen("about_screen"),
    DeveloperScreen("developer_screen")
}

val listOfNavItems = listOf(
    // Main Section
    NavItem(
        "Home",
        "Main",
        Icons.Filled.Home,
        Icons.Outlined.Home,
        Screens.HomeScreen.route
    ),
    NavItem(
        "All Transactions",
        "Main",
        Icons.Filled.ReceiptLong,
        Icons.Outlined.ReceiptLong,
        Screens.AllTransactionsScreen.route
    ),
    NavItem(
        "Category Chart",
        "Main",
        Icons.Filled.PieChart,
        Icons.Outlined.PieChart,
        Screens.CategoryChartScreen.route
    ),
    NavItem(
        "Keep Note",
        "Main",
        Icons.Filled.EditNote,
        Icons.Outlined.EditNote,
        Screens.NoteScreen.route
    ),

    // Settings Section
    NavItem(
        "Backup",
        "Settings",
        Icons.Filled.Backup,
        Icons.Outlined.Backup,
        Screens.BackupScreen.route
    ),

    // More Section
    NavItem(
        "Share App",
        "More",
        Icons.Filled.Share,
        Icons.Outlined.Share,
        Screens.ShareAppScreen.route
    ),
    NavItem(
        "About",
        "More",
        Icons.Filled.Info,
        Icons.Outlined.Info,
        Screens.AboutScreen.route
    ),
    NavItem(
        "Developer",
        "More",
        Icons.Filled.Code,
        Icons.Outlined.Code,
        Screens.DeveloperScreen.route
    )
)