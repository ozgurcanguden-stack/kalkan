package com.kalkan.app.domain.model

data class OfflineInfo(
    val id: String = "",
    val category: OfflineInfoCategory = OfflineInfoCategory.FIRST_AID,
    val title: String = "",
    val content: String = "",
    val order: Int = 0,
)

enum class OfflineInfoCategory {
    FIRST_AID,
    DISASTER_BAG,
    EMERGENCY_NUMBERS,
    ASSEMBLY_AREAS,
    SAFETY_GUIDES,
}
