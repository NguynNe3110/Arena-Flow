package com.igorwojda.showcase.feature.match.domain.model

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat

/** Match flow type per MATCH-001 §4.1. */
enum class MatchType {
    HAS_VENUE,
    NEED_VENUE,
}

/** Match lifecycle status per MATCH-001 §4.1. */
enum class MatchStatus(
    val label: String,
) {
    OPEN("Đang mở"),
    CONFIRMED("Đã ghép"),
    ARCHIVED("Đã đá"),
    COMPLETED("Hoàn thành"),
    EXPIRED("Hết hạn"),
    CANCELLED("Đã hủy"),
    ;

    companion object {
        fun fromApiName(value: String?): MatchStatus = entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: OPEN
    }
}

/** Match join request status per MATCH-001 §4.1. */
enum class MatchRequestStatus(
    val label: String,
) {
    PENDING("Chờ duyệt"),
    ACCEPTED("Đã chấp nhận"),
    REJECTED("Từ chối"),
    WITHDRAWN("Đã rút"),
    SUPERSEDED("Đã thay thế"),
    EXPIRED("Hết hạn"),
    ;

    companion object {
        fun fromApiName(value: String?): MatchRequestStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PENDING
    }
}

/** Cost agreement category per MATCH-001 §4.1. */
enum class CostType(
    val label: String,
) {
    VENUE_ONLY("Chỉ tiền sân"),
    DRINKS_ONLY("Chỉ tiền nước"),
    BOTH("Tiền sân & tiền nước"),
}

/** Cost split rule per MATCH-001 §4.1. */
enum class CostSplit(
    val label: String,
) {
    FIFTY_FIFTY("Chia đôi 50-50"),
    SEVENTY_THIRTY_HOME("70-30 (Chủ nhà 70%)"),
    SEVENTY_THIRTY_LOSER("70-30 (Đội thua 70%)"),
    LOSER_PAYS_DRINKS("Đội thua trả tiền nước"),
    ;

    companion object {
        fun labelFor(value: String?): String =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }?.label
                ?: value.orEmpty()
    }
}

/** Team summary embedded in match details. */
data class MatchTeamSummary(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val reliabilityScore: Int = 100,
)

/** Venue option or confirmed venue attached to a match. */
data class MatchVenue(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueStatus: String = "ACTIVE",
    val primaryImageUrl: String? = null,
)

/** Time slot option for flow B (NEED_VENUE) matches. */
data class MatchTimeSlot(
    val timeOptionId: String,
    val startsAt: String,
    val endsAt: String,
)

/** Custom cost line item inside a match cost agreement. */
data class MatchCostItem(
    val itemId: String = "",
    val itemName: String,
    val amount: Int,
)

/** Cost agreement attached to a match per MATCH-001 §4.2. */
data class MatchCostAgreement(
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val customItems: List<MatchCostItem> = emptyList(),
)

/** Match feed summary item per MATCH-001 §4.3. */
data class MatchSummary(
    val matchId: String,
    val type: MatchType,
    val status: String = "OPEN",
    val sportId: String = "sport-football",
    val sportName: String = "Bóng đá",
    val format: String = PitchFormat.SEVEN_A_SIDE.name,
    val skillLevels: List<String> = emptyList(),
    val startsAt: String = "",
    val endsAt: String = "",
    val expiresAt: String = "",
    val authorTeamId: String = "",
    val authorTeamName: String,
    val authorTeamReliabilityScore: Int = 100,
    val opponentTeamId: String? = null,
    val opponentTeamName: String? = null,
    val venueId: String? = null,
    val venueName: String? = null,
    val venueAddress: String? = null,
    val wardId: String? = null,
    val wardName: String? = null,
    val costSplitLabel: String? = null,
    val venueOptionCount: Int = 0,
    val timeOptionCount: Int = 0,
    val distanceKm: Double? = null,
    val createdAt: String = "",
)

/** Paginated list of match summaries. */
data class MatchPage(
    val items: List<MatchSummary>,
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalPages: Int = 1,
    val totalItems: Int = items.size,
)

/** Full match detail model per MATCH-001 §4.2. */
data class MatchDetail(
    val matchId: String,
    val type: MatchType,
    val status: String,
    val sportId: String,
    val sportName: String,
    val format: String,
    val skillLevels: List<String> = emptyList(),
    val ageNote: String? = null,
    val matchNature: String? = null,
    val startsAt: String,
    val endsAt: String,
    val expiresAt: String,
    val authorTeam: MatchTeamSummary,
    val opponentTeam: MatchTeamSummary? = null,
    val venue: MatchVenue? = null,
    val venueOptions: List<MatchVenue> = emptyList(),
    val timeOptions: List<MatchTimeSlot> = emptyList(),
    val confirmedPairingId: String? = null,
    val costAgreement: MatchCostAgreement = MatchCostAgreement(),
    val cancelledByTeamId: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
    val minReputationScore: Int? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)

/** Match join request item per MATCH-001 §4.4. */
data class MatchRequest(
    val requestId: String,
    val matchId: String,
    val conversationId: String,
    val requesterTeamId: String,
    val requesterTeamName: String,
    val requesterTeamReliabilityScore: Int,
    val status: String,
    val reason: String? = null,
    val requestedAt: String,
    val respondedAt: String? = null,
    val withdrawnAt: String? = null,
    val startsAt: String? = null,
    val authorTeamId: String? = null,
    val authorTeamName: String? = null,
)

/** Active sport option per MATCH-001 §4.6. */
data class SportOption(
    val sportId: String,
    val sportName: String,
    val isActive: Boolean,
)

/** Filter parameters for match feed discovery. */
data class MatchFilter(
    val type: MatchType? = null,
    val wardId: String? = null,
    val format: String? = null,
    val skillLevel: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val query: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
)

/** Draft payload for creating or editing a match post. */
data class MatchDraft(
    val matchId: String? = null,
    val kind: MatchType = MatchType.HAS_VENUE,
    val hostTeamId: String = "",
    val hostTeamName: String = "",
    val sportId: String = "sport-football",
    val format: String = PitchFormat.SEVEN_A_SIDE.name,
    val skillLevel: String = "Rèn luyện",
    val venueId: String? = null,
    val venueName: String? = null,
    val venueAddress: String? = null,
    val matchDate: String = "2026-11-21",
    val startTime: String = "19:00",
    val endTime: String = "20:30",
    val expiresAt: String = "2026-11-21T18:00:00",
    val proposedVenues: List<MatchVenue> = emptyList(),
    val proposedTimeSlots: List<MatchTimeSlot> = emptyList(),
    val costType: String? = "VENUE_ONLY",
    val costSplit: String = "FIFTY_FIFTY",
    val venueAmount: Int = 600_000,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val ageNote: String? = null,
    val notes: String = "",
    val minReputationScore: Int? = null,
)

/** Result returned after cancelling a match per MATCH-001 §2.5. */
data class CancelMatchResult(
    val matchId: String,
    val status: String,
    val cancelledByTeamId: String,
    val cancelledAt: String,
    val cancelReason: String?,
    val reliabilityPenalty: Int,
)

/** Result returned after accepting a match request per MATCH-001 §2.8. */
data class AcceptRequestResult(
    val matchId: String,
    val matchStatus: String,
    val requestId: String,
    val requestStatus: String,
    val opponentTeamId: String,
    val opponentTeamName: String,
    val respondedAt: String,
)

/** Result returned after withdrawing a match request per MATCH-001 §2.10. */
data class WithdrawRequestResult(
    val requestId: String,
    val matchId: String,
    val status: String,
    val withdrawnAt: String,
    val matchStatus: String,
    val reliabilityPenalty: Int,
)
