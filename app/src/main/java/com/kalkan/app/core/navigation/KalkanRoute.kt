package com.kalkan.app.core.navigation

import android.net.Uri

sealed class KalkanRoute(val route: String, val title: String) {
    data object Home : KalkanRoute("home", "Ana Sayfa")
    data object Earthquakes : KalkanRoute("earthquakes", "Depremler")
    data object Map : KalkanRoute("map", "Harita")
    data object Family : KalkanRoute("family", "Ailem")
    data object AddEmergencyContact : KalkanRoute("add_emergency_contact", "Kişi Ekle")
    data object Profile : KalkanRoute("profile", "Profil")
    data object AdminDashboard : KalkanRoute("admin_dashboard", "Admin Paneli")
    data object CreateAnnouncement : KalkanRoute("create_announcement", "Duyuru Olustur")
    data object AnnouncementDetail : KalkanRoute("announcement_detail/{announcementId}", "Duyuru Detayi") {
        fun createRoute(announcementId: String) = "announcement_detail/${Uri.encode(announcementId)}"
    }
}
