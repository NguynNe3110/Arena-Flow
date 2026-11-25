package com.igorwojda.showcase.feature.reputation.data.datasource.api.model

import kotlinx.serialization.Serializable

/** Request body for POST /api/v1/matches/{matchId}/ratings. */
@Serializable
data class SubmitMatchRatingRequestDto(
    val isNoShow: Boolean = false,
    val score: Int? = null,
    val comment: String? = null,
    val raterTeamScore: Int? = null,
    val rateeTeamScore: Int? = null,
)

/** Rating DTO per REPUTATION-001 §4.3. */
@Serializable
data class MatchRatingDto(
    val ratingId: String,
    val matchId: String,
    val raterTeamId: String,
    val rateeTeamId: String,
    val isNoShow: Boolean,
    val score: Int? = null,
    val comment: String? = null,
    val raterTeamScore: Int? = null,
    val rateeTeamScore: Int? = null,
    val createdAt: String,
    val matchStatus: String? = null,
)

/** Response DTO for GET /api/v1/matches/{matchId}/ratings. */
@Serializable
data class MatchRatingsResponseDto(
    val matchId: String,
    val teamId: String,
    val given: MatchRatingDto? = null,
    val received: MatchRatingDto? = null,
)

/** Request body for POST /api/v1/matches/{matchId}/reports. */
@Serializable
data class SubmitMatchReportRequestDto(
    val reportType: String,
    val reason: String,
)

/** Request body for POST /api/v1/conversations/{conversationId}/reports. */
@Serializable
data class SubmitChatReportRequestDto(
    val reason: String,
)

/** Compact team DTO per REPUTATION-001 §4.2. */
@Serializable
data class CounterpartTeamDto(
    val teamId: String,
    val name: String,
    val avatarUrl: String? = null,
    val status: String? = null,
)

/** Dispute nested DTO inside ReportDto per REPUTATION-001 §4.4. */
@Serializable
data class ReportDisputeDto(
    val disputeId: String,
    val status: String,
    val evidence: String? = null,
    val decision: String? = null,
    val resolutionNote: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
)

/** Report DTO per REPUTATION-001 §4.4. */
@Serializable
data class ReportDto(
    val reportId: String,
    val matchId: String,
    val matchStartsAt: String,
    val direction: String,
    val counterpartTeam: CounterpartTeamDto,
    val reportType: String,
    val reason: String,
    val status: String,
    val disputeDeadlineAt: String? = null,
    val canDispute: Boolean,
    val reliabilityPenalty: Int? = null,
    val dispute: ReportDisputeDto? = null,
    val createdAt: String,
)

/** Request body for POST /api/v1/reports/{reportId}/dispute. */
@Serializable
data class DisputeReportRequestDto(
    val evidence: String,
)

/** Response DTO for POST /api/v1/reports/{reportId}/dispute. */
@Serializable
data class DisputeCreatedDto(
    val disputeId: String,
    val reportId: String,
    val status: String,
    val evidence: String,
    val reportStatus: String,
    val createdAt: String,
)

/** Completion stats DTO inside TeamReputationDto per REPUTATION-001 §4.8. */
@Serializable
data class CompletionStatsDto(
    val completedMatches: Int,
    val countedMatches: Int,
    val completionRate: Int? = null,
    val isNewcomer: Boolean,
)

/** Rating summary DTO inside TeamReputationDto per REPUTATION-001 §4.8. */
@Serializable
data class RatingSummaryDto(
    val averageRating: Double? = null,
    val ratingCount: Int,
)

/** Verification badge DTO inside TeamReputationDto per REPUTATION-001 §4.8. */
@Serializable
data class VerificationBadgeDto(
    val isVerified: Boolean,
    val verifiedAt: String? = null,
)

/** Team reputation DTO per REPUTATION-001 §4.8. */
@Serializable
data class TeamReputationDto(
    val teamId: String,
    val reliabilityScore: Int,
    val reliabilityLevel: String,
    val completion: CompletionStatsDto,
    val rating: RatingSummaryDto,
    val verification: VerificationBadgeDto,
)

/** Admin dispute summary DTO per REPUTATION-001 §4.6. */
@Serializable
data class AdminDisputeSummaryDto(
    val disputeId: String,
    val reportId: String,
    val matchId: String,
    val matchStartsAt: String,
    val reporterTeam: CounterpartTeamDto,
    val reportedTeam: CounterpartTeamDto,
    val reportedById: String,
    val reportedByName: String,
    val isReportedByDeleted: Boolean,
    val disputedById: String,
    val disputedByName: String,
    val isDisputedByDeleted: Boolean,
    val status: String,
    val decision: String? = null,
    val resolvedById: String? = null,
    val resolvedByName: String? = null,
    val isResolvedByDeleted: Boolean,
    val resolvedAt: String? = null,
    val reportCreatedAt: String,
    val createdAt: String,
)

/** Match summary nested in AdminDisputeDetailDto per REPUTATION-001 §4.7. */
@Serializable
data class AdminDisputeMatchDto(
    val sportName: String,
    val status: String,
    val startsAt: String,
    val endsAt: String,
    val venueName: String? = null,
)

/** Admin dispute detail DTO per REPUTATION-001 §4.7. */
@Serializable
data class AdminDisputeDetailDto(
    val disputeId: String,
    val reportId: String,
    val matchId: String,
    val matchStartsAt: String,
    val reporterTeam: CounterpartTeamDto,
    val reportedTeam: CounterpartTeamDto,
    val reportedById: String,
    val reportedByName: String,
    val isReportedByDeleted: Boolean,
    val disputedById: String,
    val disputedByName: String,
    val isDisputedByDeleted: Boolean,
    val status: String,
    val decision: String? = null,
    val resolvedById: String? = null,
    val resolvedByName: String? = null,
    val isResolvedByDeleted: Boolean,
    val resolvedAt: String? = null,
    val reportCreatedAt: String,
    val createdAt: String,
    val match: AdminDisputeMatchDto,
    val reportReason: String,
    val reliabilityPenalty: Int,
    val evidence: String,
    val resolutionNote: String? = null,
    val reporterTeamReliabilityScore: Int,
    val reportedTeamReliabilityScore: Int,
    val matchRatings: List<MatchRatingDto> = emptyList(),
)

/** Request body for PATCH /api/v1/admin/disputes/{disputeId}/resolve. */
@Serializable
data class ResolveDisputeRequestDto(
    val decision: String,
    val note: String,
)
