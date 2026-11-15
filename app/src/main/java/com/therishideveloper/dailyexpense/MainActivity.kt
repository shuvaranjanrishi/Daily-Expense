package com.therishideveloper.dailyexpense

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.therishideveloper.dailyexpense.component.DrawerFooter
import com.therishideveloper.dailyexpense.component.DrawerHeader
import com.therishideveloper.dailyexpense.component.LanguageDialog
import com.therishideveloper.dailyexpense.navigation.AppNavigation
import com.therishideveloper.dailyexpense.navigation.Screens
import com.therishideveloper.dailyexpense.navigation.listOfNavItems
import com.therishideveloper.dailyexpense.screen.SplashScreen
import com.therishideveloper.dailyexpense.ui.theme.NavigationDrawerTheme
import com.therishideveloper.dailyexpense.util.LocaleHelper
import com.therishideveloper.dailyexpense.util.ThemeHelper
import com.therishideveloper.dailyexpense.util.shareApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.applyLocaleToResources(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val savedLang = LocaleHelper.getSavedLocale(this)
        LocaleHelper.applyLocaleToResources(this, savedLang)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(ThemeHelper.isDarkMode(this@MainActivity, systemDark))
            }

            NavigationDrawerTheme(darkTheme = isDarkTheme) {
                var showSplash by rememberSaveable { mutableStateOf(savedInstanceState == null) }

                LaunchedEffect(showSplash) {
                    if (showSplash) {
                        delay(4000)
                        showSplash = false
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        SetupNavigation(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = {
                                val nextTheme = !isDarkTheme
                                isDarkTheme = nextTheme
                                ThemeHelper.setDarkMode(this@MainActivity, nextTheme)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SetupNavigation(
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isHome =
            currentRoute == Screens.HomeScreen.route ||
                currentRoute == Screens.BackupScreen.route ||
                currentRoute == Screens.AboutScreen.route ||
                currentRoute == Screens.DeveloperScreen.route

        var showLanguageDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val currentLang = remember(context) { LocaleHelper.getSavedLocale(context) }

        ModalNavigationDrawer(
            gesturesEnabled = isHome,
            drawerContent = {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        DrawerHeader()

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            val groupedItems = listOfNavItems.groupBy { it.section }

                            groupedItems.forEach { (section, items) ->
                                val sectionLabel = when (section) {
                                    "Main" -> stringResource(R.string.label_main_menu)
                                    "Settings" -> stringResource(R.string.label_settings)
                                    "More" -> stringResource(R.string.label_more)
                                    else -> section
                                }

                                Text(
                                    text = sectionLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        start = 28.dp,
                                        top = 20.dp,
                                        bottom = 8.dp
                                    )
                                )

                                items.forEach { navigationItem ->
                                    val isSelected = currentRoute == navigationItem.route
                                    NavigationDrawerItem(
                                        label = {
                                            val labelText = when (navigationItem.title) {
                                                "Home" -> stringResource(R.string.menu_home)
                                                "All Transactions" -> stringResource(R.string.menu_all_transactions)
                                                "Category Chart" -> stringResource(R.string.menu_category_chart)
                                                "Keep Note" -> stringResource(R.string.menu_note)
                                                "Backup" -> stringResource(R.string.menu_backup)
                                                "Share App" -> stringResource(R.string.menu_share)
                                                "About" -> stringResource(R.string.menu_about)
                                                "Developer" -> stringResource(R.string.menu_developer)
                                                else -> navigationItem.title
                                            }
                                            Text(text = labelText)
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            scope.launch {
                                                drawerState.close()

                                                when (navigationItem.route) {
                                                    Screens.ShareAppScreen.route -> {
                                                        shareApp(context)
                                                    }

                                                    else -> {
                                                        navController.navigate(navigationItem.route) {
                                                            popUpTo(navController.graph.startDestinationId) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) navigationItem.selectedIcon else navigationItem.unselectedIcon,
                                                contentDescription = null
                                            )
                                        },
                                        modifier = Modifier
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                                            .fillMaxWidth()
                                    )
                                }

                                if (section != groupedItems.keys.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .padding(
                                                vertical = 12.dp,
                                                horizontal = 28.dp
                                            )
                                            .fillMaxWidth(),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        DrawerFooter(
                            currentLanguageCode = currentLang,
                            isDarkTheme = isDarkTheme,
                            onLanguageClick = {
                                scope.launch { drawerState.close() }
                                showLanguageDialog = true
                            },
                            onThemeToggle = onToggleTheme
                        )
                    }
                }
            },
            drawerState = drawerState
        ) {
            AppNavigation(navController, onOpenDrawer = {
                scope.launch { drawerState.open() }
            })
        }

        if (showLanguageDialog) {
            LanguageDialog(
                currentLanguageCode = currentLang,
                onDismiss = { showLanguageDialog = false },
                onLanguageSelected = { selectedCode ->
                    LocaleHelper.applyLocale(this@MainActivity, selectedCode)
                    showLanguageDialog = false
                    (context as? Activity)?.recreate()
                }
            )
        }
    }
}