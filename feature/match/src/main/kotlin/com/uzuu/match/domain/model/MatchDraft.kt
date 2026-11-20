package com.uzuu.match.domain.model

data class MatchDraft(
    val kind: MatchType = MatchType.HAS_VENUE,
    val hostTeamId: String = "team-alpha",
    val hostTeamName: String = "Bình Minh FC",
    val format: String = "SEVEN_A_SIDE",
    val skillLevel: String = "Trung bình",
    // Luồng A (HAS_VENUE)
    val venueId: String? = "ven-anphu",
    val venueName: String? = "Sân bóng An Phú",
    val venueAddress: String? = "Số 8 Đường số 2, Phường An Khánh, TP. Thủ Đức",
    val matchDate: String = "2026-11-21",
    val startTime: String = "19:00",
    val endTime: String = "20:30",
    // Luồng B (NEED_VENUE)
    val proposedVenues: List<MatchVenue> =
        listOf(
            MatchVenue("ven-anphu", "Sân bóng An Phú", "8 Đường số 2, An Khánh, TP. Thủ Đức", "ACTIVE"),
            MatchVenue("ven-binhtrung", "Sân bóng Bình Trưng", "12 Đỗ Xuân Hợp, Bình Trưng Tây, Thủ Đức", "ACTIVE"),
        ),
    val proposedTimeSlots: List<String> = listOf("17:30 - 19:00", "19:00 - 20:30"),
    val depositAmount: Int = 0,
    val depositHolder: String = "HOST",
    // Cost Agreement
    val totalCost: Int = 600000,
    val costSharing: String = "SPLIT_50_50",
    // Notes
    val notes: String = "",
)
