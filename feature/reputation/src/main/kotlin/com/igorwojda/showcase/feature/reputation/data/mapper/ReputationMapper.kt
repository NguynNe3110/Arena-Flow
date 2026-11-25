package com.igorwojda.showcase.feature.reputation.data.mapper

import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CounterpartTeamDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeCreatedDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingsResponseDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDisputeDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.TeamReputationDto
import com.igorwojda.showcase.feature.reputation.domain.model.CompletionStats
import com.igorwojda.showcase.feature.reputation.domain.model.CounterpartTeam
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeDecision
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeSubmission
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.model.RatingSummary
import com.igorwojda.showcase.feature.reputation.domain.model.ReliabilityLevel
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDispute
import com.igorwojda.showcase.feature.reputation.domain.model.ReportStatus
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import com.igorwojda.showcase.feature.reputation.domain.model.VerificationBadge

internal fun MatchRatingDto.toDomain(): MatchRating =
    MatchRating(
        ratingId = ratingId,
        matchId = matchId,
        raterTeamId = raterTeamId,
        rateeTeamId = rateeTeamId,
        isNoShow = isNoShow,
        score = score,
        comment = comment,
        raterTeamScore = raterTeamScore,
        rateeTeamScore = rateeTeamScore,
        createdAt = createdAt,
        matchStatus = matchStatus,
    )

internal fun MatchRatingsResponseDto.toDomain(): MatchRatings =
    MatchRatings(
        matchId = matchId,
        teamId = teamId,
        given = given?.toDomain(),
        received = received?.toDomain(),
    )

internal fun CounterpartTeamDto.toDomain(): CounterpartTeam =
    CounterpartTeam(
        teamId = teamId,
        name = name,
        avatarUrl = avatarUrl,
    )

internal fun ReportDisputeDto.toDomain(): ReportDispute =
    ReportDispute(
        disputeId = disputeId,
        status = status,
        evidence = evidence,
        decision = DisputeDecision.fromApiValue(decision),
        resolutionNote = resolutionNote,
        createdAt = createdAt,
        resolvedAt = resolvedAt,
    )

internal fun ReportDto.toDomain(): TeamReport =
    TeamReport(
        reportId = reportId,
        matchId = matchId,
        matchStartsAt = matchStartsAt,
        direction = ReportDirection.fromApiValue(direction),
        counterpartTeam = counterpartTeam.toDomain(),
        reportType = ReportType.fromApiValue(reportType),
        reason = reason,
        status = ReportStatus.fromApiValue(status),
        disputeDeadlineAt = disputeDeadlineAt,
        canDispute = canDispute,
        reliabilityPenalty = reliabilityPenalty,
        dispute = dispute?.toDomain(),
        createdAt = createdAt,
    )

internal fun PaginatedEnvelope<ReportDto>.toDomain(): TeamReportPage =
    TeamReportPage(
        items = data.map { it.toDomain() },
        page = pagination.page,
        pageSize = pagination.pageSize,
        totalPages = pagination.totalPages,
        totalItems = pagination.totalItems,
    )

internal fun DisputeCreatedDto.toDomain(): DisputeSubmission =
    DisputeSubmission(
        disputeId = disputeId,
        reportId = reportId,
        status = status,
        evidence = evidence,
        reportStatus = ReportStatus.fromApiValue(reportStatus),
        createdAt = createdAt,
    )

internal fun TeamReputationDto.toDomain(): TeamReputation =
    TeamReputation(
        teamId = teamId,
        reliabilityScore = reliabilityScore,
        reliabilityLevel = ReliabilityLevel.fromApiValue(reliabilityLevel, reliabilityScore),
        completion =
            CompletionStats(
                completedMatches = completion.completedMatches,
                countedMatches = completion.countedMatches,
                completionRate = completion.completionRate,
                isNewcomer = completion.isNewcomer,
            ),
        rating =
            RatingSummary(
                averageRating = rating.averageRating,
                ratingCount = rating.ratingCount,
            ),
        verification =
            VerificationBadge(
                isVerified = verification.isVerified,
                verifiedAt = verification.verifiedAt,
            ),
    )
