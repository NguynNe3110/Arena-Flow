package com.uzuu.team.data.datasource.api

import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.model.TeamMatchSchedule
import com.uzuu.team.domain.model.TeamMember
import java.util.UUID

class MockTeamDataSource : TeamRemoteDataSource {
    private var mockTeam =
        TeamDetail(
            id = "team-alpha",
            name = "Bình Minh FC",
            shortName = "BM",
            logoUrl = null,
            createdAt = "Tháng 3/2024",
            role = "Đội trưởng",
            area = "Hải Châu, Đà Nẵng",
            reputationScore = 100,
            personalReputationScore = 95,
            rating = 4.8,
            matchCount = 25,
            completionRate = 96,
            status = "ACTIVE",
            description =
                "Đội bóng sinh hoạt cuối tuần tại khu vực Hải Châu, " +
                    "ưu tiên giao lưu cọ xát văn minh, đúng giờ.",
            skillLevels = listOf("Vui vẻ", "Trung bình"),
            members =
                listOf(
                    TeamMember("m-1", "Nguyễn Văn A (Bạn)", "Đội trưởng", "Tiền vệ trung tâm", 100, isCurrentUser = true),
                    TeamMember("m-2", "Trần Văn B", "Đội phó", "Hậu vệ cánh", 95),
                    TeamMember("m-3", "Lê Văn C", "Thành viên", "Tiền đạo", 90),
                    TeamMember("m-4", "Phạm Văn D", "Thành viên", "Thủ môn", 92),
                    TeamMember("m-5", "Người dùng ArenaFlow", "Thành viên", "Tiền vệ", 85, isDeletedUser = true),
                ),
            schedules =
                listOf(
                    TeamMatchSchedule(
                        id = "sch-1",
                        time = "19:30 - 21:00, Thứ Bảy 22/11",
                        opponentName = "Sài Gòn Utd",
                        opponentShortName = "SG",
                        venueName = "Sân bóng Bình Thạnh Sport",
                        statusText = "Đang đàm phán",
                        isRecruitingMercenary = false,
                    ),
                    TeamMatchSchedule(
                        id = "sch-2",
                        time = "20:00 - 21:30, Chủ Nhật 23/11",
                        opponentName = "Chưa có đối thủ",
                        opponentShortName = "TBD",
                        venueName = "Sân Chuyên Việt",
                        statusText = "Cần tìm đối",
                        isRecruitingMercenary = true,
                        mercenaryRole = "Cần 1 Thủ môn",
                    ),
                ),
        )

    override suspend fun getMyTeam(): TeamDetail = mockTeam

    override suspend fun createTeam(draft: TeamDraft): TeamDetail {
        val newTeam =
            TeamDetail(
                id = "team-" + UUID.randomUUID().toString().take(6),
                name = draft.name,
                shortName = draft.shortName.ifBlank { draft.name.take(2).uppercase() },
                area = draft.ward.ifBlank { "Hải Châu, Đà Nẵng" },
                description = draft.description,
                skillLevels = draft.skillLevels.ifEmpty { listOf("Vui vẻ") },
                members =
                    listOf(
                        TeamMember(
                            id = "m-" + UUID.randomUUID().toString().take(4),
                            name = "Bạn",
                            role = "Đội trưởng",
                            position = "Chưa cập nhật",
                            reputationScore = 100,
                            isCurrentUser = true,
                        ),
                    ),
            )
        mockTeam = newTeam
        return newTeam
    }

    override suspend fun inviteTeamMember(
        teamId: String,
        name: String,
        position: String,
    ): TeamMember {
        val newMember =
            TeamMember(
                id = "m-" + UUID.randomUUID().toString().take(6),
                name = name,
                role = "Thành viên",
                position = position,
                reputationScore = 90,
            )
        mockTeam = mockTeam.copy(members = mockTeam.members + newMember)
        return newMember
    }
}
