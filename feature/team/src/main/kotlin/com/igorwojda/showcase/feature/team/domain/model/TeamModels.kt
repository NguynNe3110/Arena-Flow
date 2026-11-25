package com.igorwojda.showcase.feature.team.domain.model

/** Lifecycle status of a team per USER-001 §4.1 & USER-002 §5.4. */
enum class TeamStatus(
    val label: String,
) {
    ACTIVE("Hoạt động"),
    INACTIVE("Bị khóa"),
    DISBANDED("Đã giải tán"),
    ;

    companion object {
        fun fromApi(value: String): TeamStatus = entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: ACTIVE
    }
}

/** Member role inside a team per USER-001 §4.1. */
enum class TeamRole(
    val label: String,
) {
    CAPTAIN("Đội trưởng"),
    MEMBER("Thành viên"),
    ;

    companion object {
        fun fromApi(value: String): TeamRole = entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: MEMBER
    }
}

/** Official 9 skill/style labels per USER-001 §4.1. */
object OfficialSkillLabels {
    val all: List<String> =
        listOf(
            "Chill",
            "Nhiệt tình",
            "Máu lửa",
            "Vui chơi",
            "Rèn luyện",
            "Thi đấu",
            "Mới bắt đầu",
            "Đã có kinh nghiệm",
            "Lão làng",
        )
}

/** Full team detail domain model (TEAM-04 / TEAM-13). */
data class TeamDetail(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String?,
    val description: String?,
    val skillLevels: List<String>,
    val status: TeamStatus,
    val inactiveReason: String?,
    val captainId: String?,
    val captainName: String?,
    val memberCount: Int,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val disbandedAt: String?,
    val disbandedById: String?,
    val disbandedByName: String?,
    val disbandReason: String?,
)

/** Summary team item for system team directory (TEAM-02). */
data class TeamSummary(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String?,
    val skillLevels: List<String>,
    val status: TeamStatus,
    val captainName: String?,
    val memberCount: Int,
    val createdAt: String,
    val disbandedByName: String?,
)

/** Summary team item for "My Teams" (TEAM-01). */
data class MyTeam(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String?,
    val skillLevels: List<String>,
    val status: TeamStatus,
    val captainName: String?,
    val memberCount: Int,
    val role: TeamRole,
    val joinedAt: String,
)

/** Team summary nested inside a pending invitation. */
data class InvitationTeamSummary(
    val teamId: String,
    val teamName: String,
    val avatarUrl: String?,
    val skillLevels: List<String>,
    val status: TeamStatus,
    val captainName: String,
    val memberCount: Int,
)

/** Pending invitation received by the current user (TEAM-01 / TEAM-08). */
data class MyInvitation(
    val invitationId: String,
    val invitedAt: String,
    val team: InvitationTeamSummary,
)

/** Created invitation response (TEAM-07). */
data class TeamInvitation(
    val invitationId: String,
    val teamId: String,
    val teamName: String,
    val userId: String,
    val status: String,
    val invitedAt: String,
)

/** Candidate returned by invitation-candidates search (TEAM-07). */
data class InvitationCandidate(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val membershipStatus: String?,
)

/** Paginated/capped candidate search result. */
data class InvitationCandidatePage(
    val items: List<InvitationCandidate>,
    val hasMore: Boolean,
)

/** Active or historical team member (TEAM-06 / S14). */
data class TeamMember(
    val memberId: String,
    val teamId: String,
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: TeamRole,
    val status: String,
    val joinedAt: String,
    val leftAt: String? = null,
)

/** Result of captaincy transfer (TEAM-09). */
data class CaptainTransfer(
    val teamId: String,
    val fromUserId: String,
    val toUserId: String,
    val reason: String?,
    val transferredAt: String,
)

/** Entry in captaincy history log (TEAM-10). */
data class CaptainHistoryEntry(
    val historyId: String,
    val teamId: String,
    val fromUserId: String?,
    val fromUserName: String?,
    val toUserId: String,
    val toUserName: String,
    val reason: String?,
    val transferredAt: String,
)

/** Draft payload for creating or editing a team (TEAM-03 / TEAM-05). */
data class TeamDraft(
    val teamName: String,
    val avatarUrl: String? = null,
    val description: String? = null,
    val skillLevels: List<String> = emptyList(),
)
