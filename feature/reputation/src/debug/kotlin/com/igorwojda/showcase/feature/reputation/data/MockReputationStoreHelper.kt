@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "MagicNumber")

package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreMatch
import com.igorwojda.showcase.feature.base.data.store.StoreReport
import com.igorwojda.showcase.feature.base.data.store.StoreReview
import com.igorwojda.showcase.feature.base.data.store.StoreTeam
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CounterpartTeamDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDisputeDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto

internal object MockReputationStoreHelper {
    fun reliabilityLevelFor(score: Int): String =
        when {
            score >= 95 -> "EXCELLENT"
            score >= 70 -> "NORMAL"
            score >= 50 -> "WARNING"
            else -> "CRITICAL"
        }

    fun penaltyFor(
        reportType: String,
        priorCount: Int,
    ): Int {
        val index = priorCount.coerceIn(0, 3)
        return when (reportType) {
            "NOSHOW" -> listOf(5, 8, 12, 15)[index]
            "BAD_PLAY" -> listOf(3, 5, 8, 10)[index]
            "BAD_COMMUNICATION" -> listOf(2, 4, 6, 8)[index]
            "SKILL_MISMATCH" -> listOf(1, 2, 3, 4)[index]
            else -> 2
        }
    }

    fun matchStartsAt(match: StoreMatch?): String {
        if (match == null) return "2026-11-18T18:00:00"
        val time = match.startTime ?: "18:00"
        val normalizedTime = if (time.length == 5) "$time:00" else time
        return "${match.matchDate}T$normalizedTime"
    }

    fun StoreReview.toMatchRatingDto(
        match: StoreMatch?,
        matchStatus: String? = null,
    ): MatchRatingDto {
        val parts = scoreGiven?.split("-")?.mapNotNull { it.trim().toIntOrNull() }
        return MatchRatingDto(
            ratingId = id,
            matchId = matchId,
            raterTeamId = reviewerId,
            rateeTeamId = revieweeId,
            isNoShow = noShow,
            score = if (noShow) null else stars,
            comment = comment.ifBlank { null },
            raterTeamScore = parts?.getOrNull(0),
            rateeTeamScore = parts?.getOrNull(1),
            createdAt = createdAt,
            matchStatus = matchStatus ?: match?.status,
        )
    }

    fun StoreReport.toReportDto(
        snapshot: ArenaStoreSnapshot,
        viewerTeamId: String,
    ): ReportDto {
        val isSent = reporterId == viewerTeamId
        val direction = if (isSent) "SENT" else "RECEIVED"
        val counterpartId = if (isSent) reportedId else reporterId
        val counterpartTeam: StoreTeam? = snapshot.teams.firstOrNull { it.id == counterpartId }
        val match = snapshot.matches.firstOrNull { it.id == matchId }
        val disputeDto =
            disputeNote?.let { note ->
                ReportDisputeDto(
                    disputeId = "disp-$id",
                    status = if (status == "DISPUTED") "PENDING" else "RESOLVED",
                    evidence = if (isSent) null else note,
                    decision = if (status == "CONFIRMED" || status == "REJECTED") status else null,
                    resolutionNote = adminDecisionNote,
                    createdAt = createdAt,
                    resolvedAt = if (status == "CONFIRMED" || status == "REJECTED") createdAt else null,
                )
            }
        return ReportDto(
            reportId = id,
            matchId = matchId ?: "",
            matchStartsAt = matchStartsAt(match),
            direction = direction,
            counterpartTeam =
                CounterpartTeamDto(
                    teamId = counterpartId,
                    name = counterpartTeam?.name ?: "Đội đối thủ",
                    avatarUrl = null,
                ),
            reportType = type,
            reason = reason,
            status = status,
            disputeDeadlineAt = canDisputeUntil,
            canDispute = !isSent && type == "NOSHOW" && status == "AUTO_ACCEPTED" && canDisputeUntil != null,
            reliabilityPenalty = if (isSent) null else penaltyFor(type, 0),
            dispute = disputeDto,
            createdAt = createdAt,
        )
    }
}
