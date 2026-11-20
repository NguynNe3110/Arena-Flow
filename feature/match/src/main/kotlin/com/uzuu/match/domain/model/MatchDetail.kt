package com.uzuu.match.domain.model

data class MatchDetail(
    val summary: MatchSummary,
    val venueOptions: List<MatchVenue> = emptyList(),
    val timeOptions: List<MatchTimeSlot> = emptyList(),
    val costAgreement: MatchCostAgreement = MatchCostAgreement(),
    val ageNote: String? = null,
    val matchNature: String? = null,
    val cancelReason: String? = null,
)

data class MatchVenue(
    val venueId: String,
    val venueName: String,
    val address: String,
    val venueStatus: String,
)

data class MatchTimeSlot(
    val timeOptionId: String,
    val startsAt: String,
    val endsAt: String,
)

data class MatchCostAgreement(
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val customItems: List<MatchCostItem> = emptyList(),
)

data class MatchCostItem(
    val itemName: String,
    val amount: Int,
)
