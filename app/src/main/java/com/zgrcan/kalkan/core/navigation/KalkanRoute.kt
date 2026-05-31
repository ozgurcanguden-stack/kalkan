package com.zgrcan.kalkan.core.navigation

import android.net.Uri

sealed class KalkanRoute(val route: String, val title: String) {
    data object Home : KalkanRoute("home", "Ana Sayfa")
    data object Earthquakes : KalkanRoute("earthquakes", "Depremler")
    data object EarthquakeDetail : KalkanRoute("earthquake_detail/{earthquakeId}", "Deprem Detayı") {
        fun createRoute(earthquakeId: String) = "earthquake_detail/${Uri.encode(earthquakeId)}"
    }
    data object Map : KalkanRoute("map", "Harita")
    data object Family : KalkanRoute("family", "Ailem")
    data object AddEmergencyContact : KalkanRoute("add_emergency_contact", "Kişi Ekle")
    data object EmergencyProfileView : KalkanRoute("emergency_profile", "Acil Durum Kartı")
    data object EmergencyProfileEdit : KalkanRoute("emergency_profile/edit", "Kartı Düzenle")
    data object Profile : KalkanRoute("profile", "Profil")
    data object AdminDashboard : KalkanRoute("admin_dashboard", "Admin Paneli")
    data object CreateAnnouncement : KalkanRoute("create_announcement", "Duyuru Olustur")
    data object AdminEmergencyAlert : KalkanRoute("admin_emergency_alert", "Acil Uyari Yayinla")
    data object AdminUsers : KalkanRoute("admin_users", "Kullanicilar")
    data object AdminNotifications : KalkanRoute("admin_notifications", "Bildirim Merkezi")
    data object AdminEarthquakeMonitor : KalkanRoute("admin_earthquake_monitor", "Deprem Izleme")
    data object AdminSystemMonitor : KalkanRoute("admin_system_monitor", "Sistem Izleme")
    data object AnnouncementDetail : KalkanRoute("announcement_detail/{announcementId}", "Duyuru Detayi") {
        fun createRoute(announcementId: String) = "announcement_detail/${Uri.encode(announcementId)}"
    }
}
