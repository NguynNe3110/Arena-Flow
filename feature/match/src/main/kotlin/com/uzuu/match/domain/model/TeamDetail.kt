package com.uzuu.match.domain.model

data class TeamDetail(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String? = null,
    val createdAt: String = "Tháng 3/2024",
    val role: String = "Đội trưởng",
    val area: String = "Hải Châu, Đà Nẵng",
    val reputationScore: Int = 100,
    val personalReputationScore: Int = 95,
    val rating: Double = 4.8,
    val matchCount: Int = 25,
    val completionRate: Int = 96,
    val status: String = "ACTIVE",
    val description: String = "",
    val skillLevels: List<String> = listOf("Vui vẻ", "Trung bình"),
    val members: List<TeamMember> = emptyList(),
    val schedules: List<TeamMatchSchedule> = emptyList(),
)

data class TeamMember(
    val id: String,
    val name: String,
    val role: String = "Thành viên",
    val position: String = "Tiền vệ trung tâm",
    val reputationScore: Int = 90,
    val isCurrentUser: Boolean = false,
    val isDeletedUser: Boolean = false,
) {
    val displayName: String
        get() = if (isDeletedUser) "Người dùng ArenaFlow" else name
}

data class TeamMatchSchedule(
    val id: String,
    val time: String,
    val opponentName: String,
    val opponentShortName: String,
    val venueName: String,
    val statusText: String,
    val isRecruitingMercenary: Boolean = false,
    val mercenaryRole: String? = null,
)
