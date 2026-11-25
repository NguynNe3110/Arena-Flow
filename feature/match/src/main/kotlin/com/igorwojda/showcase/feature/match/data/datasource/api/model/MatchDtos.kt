@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.match.data.datasource.api.model

import kotlinx.serialization.Serializable

/** Team summary DTO inside match responses. */
@Serializable
data class MatchTeamSummaryDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val reliabilityScore: Int = 100,
)

/** Venue summary DTO inside match responses. */
@Serializable
data class MatchVenueDto(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueStatus: String = "ACTIVE",
    val primaryImageUrl: String? = null,
)

/** Time option DTO for flow B matches. */
@Serializable
data class MatchTimeOptionDto(
    val timeOptionId: String,
    val startsAt: String,
    val endsAt: String,
)

/** Custom cost item DTO inside cost agreement. */
@Serializable
data class MatchCostItemDto(
    val itemId: String = "",
    val itemName: String,
    val amount: Int,
)

/** Custom cost item request DTO. */
@Serializable
data class MatchCostItemRequestDto(
    val itemName: String,
    val amount: Int,
)

/** Cost agreement DTO embedded in match detail. */
@Serializable
data class MatchCostAgreementDto(
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val customItems: List<MatchCostItemDto> = emptyList(),
)

/** Match feed summary DTO per MATCH-001 §4.3. */
@Serializable
data class MatchSummaryDto(
    val matchId: String,
    val type: String,
    val status: String = "OPEN",
    val sportId: String = "sport-football",
    val sportName: String = "Bóng đá",
    val format: String = "SEVEN_A_SIDE",
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
    val venueOptionCount: Int? = null,
    val timeOptionCount: Int? = null,
    val distanceKm: Double? = null,
    val createdAt: String = "",
)

/** Full match detail DTO per MATCH-001 §4.2. */
@Serializable
data class MatchDetailDto(
    val matchId: String,
    val type: String,
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
    val authorTeam: MatchTeamSummaryDto,
    val opponentTeam: MatchTeamSummaryDto? = null,
    val venue: MatchVenueDto? = null,
    val venueOptions: List<MatchVenueDto> = emptyList(),
    val timeOptions: List<MatchTimeOptionDto> = emptyList(),
    val confirmedPairingId: String? = null,
    val costAgreement: MatchCostAgreementDto = MatchCostAgreementDto(),
    val cancelledByTeamId: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
    val minReputationScore: Int? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)

/** Time option payload when creating a flow B match. */
@Serializable
data class CreateMatchTimeOptionDto(
    val startsAt: String,
    val endsAt: String,
)

/** Request body for POST /matches per MATCH-001 §2.1. */
@Serializable
data class CreateMatchRequestDto(
    val type: String = "HAS_VENUE",
    val teamId: String,
    val sportId: String,
    val venueId: String? = null,
    val venueOptionIds: List<String>? = null,
    val format: String,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val timeOptions: List<CreateMatchTimeOptionDto>? = null,
    val expiresAt: String,
    val skillLevels: List<String> = emptyList(),
    val ageNote: String? = null,
    val matchNature: String? = null,
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val depositAmount: Int? = null,
    val depositHolder: String? = null,
    val minReputationScore: Int? = null,
)

/** Request body for PUT /matches/{matchId}. */
@Serializable
data class UpdateMatchRequestDto(
    val format: String,
    val skillLevels: List<String> = emptyList(),
    val venueId: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val expiresAt: String? = null,
    val ageNote: String? = null,
    val matchNature: String? = null,
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val minReputationScore: Int? = null,
)

/** Request body for PUT /matches/{matchId}/cost-agreement per MATCH-001 §2.4. */
@Serializable
data class CostAgreementRequestDto(
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val customItems: List<MatchCostItemRequestDto> = emptyList(),
)

/** Response body for PUT /matches/{matchId}/cost-agreement per MATCH-001 §4.5. */
@Serializable
data class CostAgreementResponseDto(
    val matchId: String,
    val costType: String? = null,
    val costSplit: String? = null,
    val venueAmount: Int? = null,
    val drinksAmount: Int? = null,
    val customItems: List<MatchCostItemDto> = emptyList(),
    val updatedAt: String = "",
)

/** Request body for POST /matches/{matchId}/cancel per MATCH-001 §2.5. */
@Serializable
data class CancelMatchRequestDto(
    val teamId: String,
    val expectedStatus: String,
    val reason: String? = null,
)

/** Response body for POST /matches/{matchId}/cancel per MATCH-001 §2.5. */
@Serializable
data class CancelMatchResponseDto(
    val matchId: String,
    val status: String,
    val cancelledByTeamId: String,
    val cancelledAt: String,
    val cancelReason: String? = null,
    val reliabilityPenalty: Int = 0,
)

/** Request body for POST /matches/{matchId}/forfeit. */
@Serializable
data class ForfeitMatchRequestDto(
    val teamId: String,
    val reason: String? = null,
)

/** Request body for POST /matches/{matchId}/requests per MATCH-001 §2.6. */
@Serializable
data class SendMatchRequestDto(
    val teamId: String,
    val message: String? = null,
)

/** Match request DTO per MATCH-001 §4.4. */
@Serializable
data class MatchRequestDto(
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

/** Response body for POST /match-requests/{requestId}/accept per MATCH-001 §2.8. */
@Serializable
data class AcceptMatchRequestResponseDto(
    val matchId: String,
    val matchStatus: String,
    val requestId: String,
    val requestStatus: String,
    val opponentTeamId: String,
    val opponentTeamName: String,
    val respondedAt: String,
)

/** Request body for POST /match-requests/{requestId}/reject per MATCH-001 §2.9. */
@Serializable
data class RejectMatchRequestDto(
    val reason: String? = null,
)

/** Request body for POST /match-requests/{requestId}/withdraw per MATCH-001 §2.10. */
@Serializable
data class WithdrawMatchRequestDto(
    val expectedStatus: String,
    val reason: String? = null,
)

/** Response body for POST /match-requests/{requestId}/withdraw per MATCH-001 §2.10. */
@Serializable
data class WithdrawMatchRequestResponseDto(
    val requestId: String,
    val matchId: String,
    val status: String,
    val withdrawnAt: String,
    val matchStatus: String,
    val reliabilityPenalty: Int = 0,
)

/** Sport catalog DTO per MATCH-001 §4.6. */
@Serializable
data class SportDto(
    val sportId: String,
    val sportName: String,
    val isActive: Boolean,
    val createdAt: String = "",
)
