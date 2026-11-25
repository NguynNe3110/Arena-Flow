@file:Suppress("TooManyFunctions")

package com.igorwojda.showcase.feature.team.data.mapper

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainHistoryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainTransferDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationCandidateDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationCandidateListDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationTeamSummaryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyInvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyTeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamMemberDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamSummaryDto
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.CaptainTransfer
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidate
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidatePage
import com.igorwojda.showcase.feature.team.domain.model.InvitationTeamSummary
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary

internal inline fun <T, R> ApiResult<T>.toDomainResult(mapper: (T) -> R): Result<R> =
    when (this) {
        is ApiResult.Success -> {
            Result.Success(mapper(data))
        }

        is ApiResult.Error -> {
            Result.Failure(apiErrorToAppError(code, errorBody))
        }

        is ApiResult.Exception -> {
            val error =
                (throwable as? Exception)?.toAppError() ?: AppError.Unknown
            Result.Failure(error)
        }
    }

internal fun TeamDto.toDomain(): TeamDetail =
    TeamDetail(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        description = description,
        skillLevels = skillLevels,
        status = TeamStatus.fromApi(status),
        inactiveReason = inactiveReason,
        captainId = captainId,
        captainName = captainName,
        memberCount = memberCount,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        disbandedAt = disbandedAt,
        disbandedById = disbandedById,
        disbandedByName = disbandedByName,
        disbandReason = disbandReason,
    )

internal fun TeamSummaryDto.toDomain(): TeamSummary =
    TeamSummary(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        skillLevels = skillLevels,
        status = TeamStatus.fromApi(status),
        captainName = captainName,
        memberCount = memberCount,
        createdAt = createdAt,
        disbandedByName = disbandedByName,
    )

internal fun MyTeamDto.toDomain(): MyTeam =
    MyTeam(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        skillLevels = skillLevels,
        status = TeamStatus.fromApi(status),
        captainName = captainName,
        memberCount = memberCount,
        role = TeamRole.fromApi(role),
        joinedAt = joinedAt,
    )

internal fun InvitationTeamSummaryDto.toDomain(): InvitationTeamSummary =
    InvitationTeamSummary(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        skillLevels = skillLevels,
        status = TeamStatus.fromApi(status),
        captainName = captainName,
        memberCount = memberCount,
    )

internal fun MyInvitationDto.toDomain(): MyInvitation =
    MyInvitation(
        invitationId = invitationId,
        invitedAt = invitedAt,
        team = team.toDomain(),
    )

internal fun InvitationDto.toDomain(): TeamInvitation =
    TeamInvitation(
        invitationId = invitationId,
        teamId = teamId,
        teamName = teamName,
        userId = userId,
        status = status,
        invitedAt = invitedAt,
    )

internal fun InvitationCandidateDto.toDomain(): InvitationCandidate =
    InvitationCandidate(
        userId = userId,
        displayName = displayName,
        avatarUrl = avatarUrl,
        membershipStatus = membershipStatus,
    )

internal fun InvitationCandidateListDto.toDomain(): InvitationCandidatePage =
    InvitationCandidatePage(
        items = items.map { it.toDomain() },
        hasMore = hasMore,
    )

internal fun TeamMemberDto.toDomain(): TeamMember =
    TeamMember(
        memberId = memberId,
        teamId = teamId,
        userId = userId,
        displayName = displayName,
        avatarUrl = avatarUrl,
        role = TeamRole.fromApi(role),
        status = status,
        joinedAt = joinedAt,
        leftAt = leftAt,
    )

internal fun CaptainTransferDto.toDomain(): CaptainTransfer =
    CaptainTransfer(
        teamId = teamId,
        fromUserId = fromUserId,
        toUserId = toUserId,
        reason = reason,
        transferredAt = transferredAt,
    )

internal fun CaptainHistoryDto.toDomain(): CaptainHistoryEntry =
    CaptainHistoryEntry(
        historyId = historyId,
        teamId = teamId,
        fromUserId = fromUserId,
        fromUserName = fromUserName,
        toUserId = toUserId,
        toUserName = toUserName,
        reason = reason,
        transferredAt = transferredAt,
    )
