package com.alaa.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alaa.presentation.challenges.ChallengesScreen
import com.alaa.presentation.dhikr.DhikrScreen
import com.alaa.presentation.dhikr.AdhkarScreen
import com.alaa.presentation.home.HomeScreen
import com.alaa.presentation.mesbaha.MesbahaScreen
import com.alaa.presentation.prayer.PrayerTimesScreen
import com.alaa.presentation.quran.QuranScreen
import com.alaa.presentation.screen.radio.RadioScreen
import com.alaa.presentation.screen.settings.SettingsScreen
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import com.alaa.presentation.mushaf.MushafScreen

data class BottomNavItem(val label: String, val icon: ImageVector, val screen: Screen)

private val navItems = listOf(
    BottomNavItem("الرئيسية",    Icons.Default.Home,         Screen.Home),
    BottomNavItem("الأذكار الصوتيه",     Icons.Default.MusicNote,    Screen.Dhikr),
    BottomNavItem("السبحة",     Icons.Default.Star,          Screen.Mesbaha),
   BottomNavItem("أذكار",       Icons.Default.Star, Screen.Adhkar),
    BottomNavItem("المصحف",   Icons.Default.MenuBook, Screen.Mushaf),
    //BottomNavItem("القرآن",   Icons.Default.MenuBook,      Screen.Quran),
    BottomNavItem("راديو",      Icons.Default.Radio, Screen.Radio),
    
    //BottomNavItem("تحديات", Icons.Default.FitnessCenter, Screen.Challenges),
)


// الشاشات اللي بيظهر فيها الـ bottom bar
private val bottomBarScreens = navItems.map { it.screen.route }

@Composable
fun AppNavHost(startScreen: String? = null) {
    val navController = rememberNavController()
    val backStack     by navController.currentBackStackEntryAsState()
    val currentRoute  = backStack?.destination?.route
LaunchedEffect(startScreen) {
    if (!startScreen.isNullOrBlank()) {
        navController.navigate(startScreen) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }
}
    
    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                NavigationBar(containerColor = DarkBg2) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, null, tint = if (selected) Gold else Color.Gray) },
                            label = { Text(item.label, fontSize = 10.sp, color = if (selected) Gold else Color.Gray) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Gold.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route)        { HomeScreen(navController) }
            composable(Screen.PrayerTimes.route) { PrayerTimesScreen() }
            composable(Screen.Dhikr.route)       { DhikrScreen() }
            composable(Screen.Mesbaha.route)     { MesbahaScreen() }
            composable(Screen.Mushaf.route)      { MushafScreen() }
            //composable(Screen.Quran.route)       { QuranScreen() }
            composable(Screen.Radio.route)       { RadioScreen() }   // ← جديد
           composable(Screen.Adhkar.route)       { AdhkarScreen() }
            composable(Screen.Settings.route)    { SettingsScreen() }
           //composable(Screen.Challenges.route) { ChallengesScreen() }
        }
    }
}
