package com.uzuu.match.data.datasource.api.response

import kotlinx.serialization.Serializable

@Serializable
internal data class MatchDetailResponse(val data: MatchDetailDto)

@Serializable
internal data class MatchDetailDto(
    val matchId: String,
    val type: String,
    val status: String,
    val format: String,
    val skillLevels: List<String> = emptyList(),
    val startsAt: String,
    val endsAt: String,
    val expiresAt: String,
    val authorTeam: MatchTeamDto,
    val opponentTeam: MatchTeamDto? = null,
    val venue: MatchVenueDto? = null,
    val venueOptions: List<MatchVenueDto> = emptyList(),
    val timeOptions: List<MatchTimeSlotDto> = emptyList(),
    val costAgreement: MatchCostAgreementDto = MatchCostAgreementDto(),
    val ageNote: String? = null,
    val matchNature: String? = null,
    val cancelReason: String? = null,
)

@Serializable
internal data class MatchTeamDto(val teamId: String, val teamName: String, val reliabilityScore: Int)

@Serializable
internal data class MatchVenueDto(
    val venueId: String,
    val venueName: String,
    val address: String,
    val venueStatus: String = "ACTIVE",
)

@Serializable
internal data class MatchTimeSlotDto(val timeOptionId: String, val startsAt: String, val endsAt: String)

@Serializable
internal data class MatchCostAgreementDto(
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val customItems: List<MatchCostItemDto> = emptyList(),
)

@Serializable
internal data class MatchCostItemDto(val itemName: String, val amount: Int)
