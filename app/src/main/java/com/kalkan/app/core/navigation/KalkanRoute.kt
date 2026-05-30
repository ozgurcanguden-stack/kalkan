package com.kalkan.app.core.navigation

sealed class KalkanRoute(val route: String, val title: String) {
    data object Home : KalkanRoute("home", "Ana Sayfa")
    data object Earthquakes : KalkanRoute("earthquakes", "Depremler")
    data object Map : KalkanRoute("map", "Harita")
    data object Family : KalkanRoute("family", "Ailem")
    data object Profile : KalkanRoute("profile", "Profil")
    data object AdminDashboard : KalkanRoute("admin_dashboard", "Admin Paneli")
}
