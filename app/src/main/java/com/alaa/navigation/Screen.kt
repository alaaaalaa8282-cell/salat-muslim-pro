package com.alaa.navigation

sealed class Screen(val route: String) {
    data object Home         : Screen("home")
    data object PrayerTimes  : Screen("prayer_times")
    data object Dhikr        : Screen("dhikr")
    data object Adhkar      : Screen("adhkar")
    data object Mesbaha      : Screen("mesbaha")
    data object Mushaf      : Screen("mushaf")
    //data object Quran        : Screen("quran")
    data object Radio        : Screen("radio")  
    data object Settings     : Screen("settings")
   //data object Challenges : Screen("challenges")
}

