package com.therishideveloper.dailyexpense

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.therishideveloper.dailyexpense.component.DrawerHeader
import com.therishideveloper.dailyexpense.component.LanguageDialog
import com.therishideveloper.dailyexpense.navigation.AppNavigation
import com.therishideveloper.dailyexpense.navigation.Screens
import com.therishideveloper.dailyexpense.navigation.listOfNavItems
import com.therishideveloper.dailyexpense.ui.theme.NavigationDrawerTheme
import com.therishideveloper.dailyexpense.ui.theme.tealColor
import com.therishideveloper.dailyexpense.util.LocaleHelper
import com.therishideveloper.dailyexpense.util.shareApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val savedLang = LocaleHelper.getSavedLocale(this)
        LocaleHelper.applyLocale(this,savedLang)

        setContent {
            NavigationDrawerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SetupNavigation()
                }
            }
//
        }
    }

    @Composable
    private fun SetupNavigation() {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isHome =
            currentRoute == Screens.HomeScreen.route || currentRoute == Screens.BackupScreen.route || currentRoute == Screens.AboutScreen.route

        var showLanguageDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

        ModalNavigationDrawer(
            gesturesEnabled = isHome,
            drawerContent = {
                ModalDrawerSheet (
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DrawerHeader()

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
                                color = tealColor,
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
                                            "Language" -> stringResource(R.string.menu_language)
                                            "Backup" -> stringResource(R.string.menu_backup)
                                            "Share App" -> stringResource(R.string.menu_share)
                                            "About" -> stringResource(R.string.menu_about)
                                            else -> navigationItem.title
                                        }
                                        Text(text = labelText)
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()

                                            when (navigationItem.route) {
                                                Screens.LanguageScreen.route -> {
                                                    showLanguageDialog = true
                                                }

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
                                        .fillMaxWidth(0.7f),
                                    thickness = 0.5.dp,
                                    color = Color.Gray.copy(alpha = 0.2f)
                                )
                            }
                        }
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
                currentLanguageCode = LocaleHelper.getSavedLocale(context),
                onDismiss = { showLanguageDialog = false },
                onLanguageSelected = { selectedCode ->
                    LocaleHelper.applyLocale(this,selectedCode)
                    (context as? Activity)?.recreate()
                    showLanguageDialog = false
                }
            )
        }
    }
}