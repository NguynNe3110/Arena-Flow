package com.igorwojda.showcase.feature.team.data.datasource.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateTeamRequestDto(
    val teamName: String,
    val avatarUrl: String? = null,
    val description: String? = null,
    val skillLevels: List<String> = emptyList(),
)

@Serializable
data class UpdateTeamRequestDto(
    val teamName: String? = null,
    val avatarUrl: String? = null,
    val description: String? = null,
    val skillLevels: List<String>? = null,
)

@Serializable
data class DisbandTeamRequestDto(
    val reason: String? = null,
)

@Serializable
data class InviteMemberRequestDto(
    val userId: String,
)

@Serializable
data class TransferCaptainRequestDto(
    val newCaptainId: String,
    val reason: String? = null,
)

@Serializable
data class TeamDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val description: String? = null,
    val skillLevels: List<String> = emptyList(),
    val status: String,
    val inactiveReason: String? = null,
    val captainId: String? = null,
    val captainName: String? = null,
    val memberCount: Int,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val disbandedAt: String? = null,
    val disbandedById: String? = null,
    val disbandedByName: String? = null,
    val disbandReason: String? = null,
)

@Serializable
data class TeamSummaryDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val skillLevels: List<String> = emptyList(),
    val status: String,
    val captainName: String? = null,
    val memberCount: Int,
    val createdAt: String,
    val disbandedByName: String? = null,
)

@Serializable
data class MyTeamDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val skillLevels: List<String> = emptyList(),
    val status: String,
    val captainName: String? = null,
    val memberCount: Int,
    val role: String,
    val joinedAt: String,
)

@Serializable
data class InvitationTeamSummaryDto(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String? = null,
    val skillLevels: List<String> = emptyList(),
    val status: String,
    val captainName: String,
    val memberCount: Int,
)

@Serializable
data class MyInvitationDto(
    val invitationId: String,
    val invitedAt: String,
    val team: InvitationTeamSummaryDto,
)

@Serializable
data class InvitationDto(
    val invitationId: String,
    val teamId: String,
    val teamName: String,
    val userId: String,
    val status: String,
    val invitedAt: String,
)

@Serializable
data class InvitationCandidateDto(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val membershipStatus: String? = null,
)

@Serializable
data class InvitationCandidateListDto(
    val items: List<InvitationCandidateDto> = emptyList(),
    val hasMore: Boolean = false,
)

@Serializable
data class TeamMemberDto(
    val memberId: String,
    val teamId: String,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String,
    val status: String,
    val joinedAt: String,
    val leftAt: String? = null,
)

@Serializable
data class CaptainTransferDto(
    val teamId: String,
    val fromUserId: String,
    val toUserId: String,
    val reason: String? = null,
    val transferredAt: String,
)

@Serializable
data class CaptainHistoryDto(
    val historyId: String,
    val teamId: String,
    val fromUserId: String? = null,
    val fromUserName: String? = null,
    val toUserId: String,
    val toUserName: String,
    val reason: String? = null,
    val transferredAt: String,
)
