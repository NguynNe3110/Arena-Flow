package com.uzuu.match.data.datasource.api

import android.content.Context
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.MatchDetailDto
import com.uzuu.match.data.datasource.api.response.MatchDetailResponse
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.response.MatchTeamDto
import com.uzuu.match.data.datasource.api.response.MatchVenueDto
import com.uzuu.match.data.datasource.api.response.MatchCostAgreementDto
import com.uzuu.match.data.datasource.api.service.MatchApi
import com.uzuu.match.domain.model.MatchVenue
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.model.PairingChatMessage
import com.uzuu.match.domain.model.PairingRoom
import com.uzuu.match.domain.model.PairingTeam
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.model.TeamMatchSchedule
import com.uzuu.match.domain.model.TeamMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArrayList

internal class MockMatchDataSource(
    private val context: Context,
    private val json: Json,
) : MatchApi {
    private val addedMatches = CopyOnWriteArrayList<MatchSummaryDto>()
    private val addedDetails = CopyOnWriteArrayList<MatchDetailDto>()

    private var mockPairingRoom = PairingRoom(
        pairingId = "pairing-beta-alpha",
        matchId = "01933b6e-8f10-7a2c-9d41-2f6c1a7e5b02",
        code = "#SG-BM-2111",
        hostTeam = PairingTeam("team-beta", "Sài Gòn Utd", "SG", 95, "Chưa có sân"),
        guestTeam = PairingTeam("team-alpha", "Bình Minh FC", "BM", 98, "Đang đàm phán"),
        version = "v1.2",
        venueOptions = listOf(
            MatchVenue("ven-binhthanh", "Sân bóng Bình Thạnh Sport", "Hẻm 48 Điện Biên Phủ, P.25, Bình Thạnh", "ACTIVE"),
            MatchVenue("ven-anphu", "Sân bóng An Phú", "Đường số 12, P. An Khánh, TP. Thủ Đức", "ACTIVE"),
        ),
        selectedVenueId = "ven-binhthanh",
        timeOptions = listOf("19:30 - 21:00", "20:30 - 22:00"),
        selectedTimeSlot = "19:30 - 21:00",
        totalCost = 600000,
        costSharing = "SPLIT_50_50",
        depositAmount = 200000,
        depositHolder = "Sài Gòn Utd tạm giữ trực tiếp",
        hostConfirmed = true,
        guestConfirmed = false,
        chatMessages = listOf(
            PairingChatMessage("1", "Sài Gòn Utd", "Chào Bình Minh FC! Đội mình muốn đá sân Bình Thạnh khung 19:30 nhé.", "10:15", false),
            PairingChatMessage("2", "Bình Minh FC", "Chào bạn, khung giờ này đội mình đá được. Đã kiểm tra chi phí.", "10:18", true),
        ),
    )

    private val mockMercenaryPosts = CopyOnWriteArrayList<MercenaryPost>().apply {
        addAll(
            listOf(
                MercenaryPost(
                    id = "rec-1",
                    teamName = "Bình Minh FC",
                    captainName = "Lê Hoàng",
                    reliabilityScore = 92,
                    totalMatches = 34,
                    position = "GK",
                    positionLabel = "[CẦN 1 THỦ MÔN - GK]",
                    matchDate = "Thứ Bảy, 21/11/2026",
                    timeSlot = "19:00 – 20:30 (90 phút)",
                    estimatedArea = "P. An Khánh, TP. HCM",
                    distanceKm = 2.5,
                    costShare = "Tiền sân đội bao hoàn toàn hoặc chia nhẹ tùy trận, có sẵn nước đá.",
                    requirements = "Trình độ trung bình, bắt bóng an toàn, có găng tay riêng.",
                    openSlots = 1,
                    pendingApplicants = 2,
                    status = "OPEN",
                ),
                MercenaryPost(
                    id = "rec-2",
                    teamName = "Gia Định FC",
                    captainName = "Minh Triết",
                    reliabilityScore = 88,
                    totalMatches = 28,
                    position = "MF",
                    positionLabel = "[CẦN 2 TIỀN VỆ - MF]",
                    matchDate = "Chủ Nhật, 22/11/2026",
                    timeSlot = "18:00 – 19:30 (90 phút)",
                    estimatedArea = "P. Bình Trưng Tây, TP. Thủ Đức",
                    distanceKm = 3.8,
                    costShare = "Chia 50k/người bao gồm nước uống.",
                    requirements = "Ưu tiên thể lực tốt, chuyền bóng chắc chắn.",
                    openSlots = 2,
                    pendingApplicants = 1,
                    status = "OPEN",
                ),
            ),
        )
    }

    private var mockTeam = TeamDetail(
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
        description = "Đội bóng phong trào giao lưu cuối tuần vui vẻ, văn minh, fair-play.",
        skillLevels = listOf("Vui vẻ", "Trung bình"),
        members = listOf(
            TeamMember("m-1", "Lê Hoàng", "Đội trưởng", "Tiền vệ trung tâm", 95, isCurrentUser = true, isDeletedUser = false),
            TeamMember("m-2", "Trần Minh", "Thành viên", "Hậu vệ", 88, isCurrentUser = false, isDeletedUser = false),
            TeamMember("m-3", "Nguyễn Đức", "Thành viên", "Tiền đạo", 91, isCurrentUser = false, isDeletedUser = false),
            TeamMember("m-4", "Cựu Cầu Thủ", "Thành viên", "Hậu vệ cánh", 70, isCurrentUser = false, isDeletedUser = true),
        ),
        schedules = listOf(
            TeamMatchSchedule("sch-1", "19:30 • 21/11/2026", "Sài Gòn United", "SGU", "Sân Thảo Điền 2, Thủ Đức", "Đang thương lượng"),
            TeamMatchSchedule("sch-2", "17:00 • 24/11/2026", "Nội bộ chiều Thứ 7", "NB", "Sân 7 người", "Đang tuyển 1 GK", isRecruitingMercenary = true, mercenaryRole = "Cần 1 Thủ Môn"),
            TeamMatchSchedule("sch-3", "18:00 • 15/11/2026", "FC Cơn Lốc", "CL", "Sân Chuyên Việt", "Đã kết thúc"),
        ),
    )

    override suspend fun getMatch(matchId: String): ApiResult<MatchDetailResponse> =
        withContext(Dispatchers.IO) {
            val added = addedDetails.find { it.matchId == matchId }
            if (added != null) return@withContext ApiResult.Success(MatchDetailResponse(added))

            val content = context.assets.open("mock/match-details.json").bufferedReader().use { it.readText() }
            val match = json.decodeFromString<List<MatchDetailDto>>(content).find { it.matchId == matchId }
            if (match == null) ApiResult.Error(404, "Không tìm thấy kèo")
            else ApiResult.Success(MatchDetailResponse(match))
        }

    override suspend fun getMatches(): ApiResult<MatchListResponse> {
        return withContext(Dispatchers.IO) {
            val content = context.assets
                .open("mock/matches.json")
                .bufferedReader()
                .use { reader -> reader.readText() }

            val response =
                json.decodeFromString<MatchListResponse>(content)

            val combined = addedMatches.toList() + response.data
            ApiResult.Success(
                response.copy(
                    data = combined,
                    pagination = response.pagination.copy(totalItems = combined.size),
                ),
            )
        }
    }

    override suspend fun createMatch(draftDto: MatchSummaryDto): ApiResult<MatchSummaryDto> =
        withContext(Dispatchers.IO) {
            addedMatches.add(0, draftDto)
            addedDetails.add(
                0,
                MatchDetailDto(
                    matchId = draftDto.matchId,
                    type = draftDto.type,
                    status = draftDto.status,
                    format = draftDto.format,
                    skillLevels = draftDto.skillLevels,
                    startsAt = draftDto.startsAt,
                    endsAt = draftDto.endsAt,
                    expiresAt = draftDto.expiresAt,
                    authorTeam = MatchTeamDto(draftDto.authorTeamId, draftDto.authorTeamName, draftDto.authorTeamReliabilityScore),
                    venue = if (draftDto.venueId != null) MatchVenueDto(draftDto.venueId, draftDto.venueName ?: "", draftDto.venueAddress ?: "") else null,
                    costAgreement = MatchCostAgreementDto(),
                ),
            )
            ApiResult.Success(draftDto)
        }

    override suspend fun getPairingRoom(pairingId: String): ApiResult<PairingRoom> =
        withContext(Dispatchers.IO) {
            ApiResult.Success(mockPairingRoom.copy(pairingId = pairingId))
        }

    override suspend fun confirmPackage(pairingId: String): ApiResult<PairingRoom> =
        withContext(Dispatchers.IO) {
            mockPairingRoom = mockPairingRoom.copy(guestConfirmed = true)
            ApiResult.Success(mockPairingRoom)
        }

    override suspend fun updatePackageOption(
        pairingId: String,
        venueId: String?,
        timeSlot: String?,
    ): ApiResult<PairingRoom> =
        withContext(Dispatchers.IO) {
            // AC-P04: when an option changes, confirmations reset!
            mockPairingRoom = mockPairingRoom.copy(
                selectedVenueId = venueId ?: mockPairingRoom.selectedVenueId,
                selectedTimeSlot = timeSlot ?: mockPairingRoom.selectedTimeSlot,
                hostConfirmed = false,
                guestConfirmed = false,
                version = "v1.3",
            )
            ApiResult.Success(mockPairingRoom)
        }

    override suspend fun getMercenaryPosts(): ApiResult<List<MercenaryPost>> =
        withContext(Dispatchers.IO) {
            ApiResult.Success(mockMercenaryPosts.toList())
        }

    override suspend fun applyMercenary(postId: String, message: String): ApiResult<MercenaryPost> =
        withContext(Dispatchers.IO) {
            val index = mockMercenaryPosts.indexOfFirst { it.id == postId }
            if (index != -1) {
                val current = mockMercenaryPosts[index]
                val updated = current.copy(isApplied = true, pendingApplicants = current.pendingApplicants + 1)
                mockMercenaryPosts[index] = updated
                ApiResult.Success(updated)
            } else {
                ApiResult.Error(404, "Không tìm thấy tin tuyển người")
            }
        }

    override suspend fun getMyTeam(): ApiResult<TeamDetail> =
        withContext(Dispatchers.IO) {
            ApiResult.Success(mockTeam)
        }

    override suspend fun createTeam(draft: TeamDraft): ApiResult<TeamDetail> =
        withContext(Dispatchers.IO) {
            val created = TeamDetail(
                id = "team-${System.currentTimeMillis()}",
                name = draft.name,
                shortName = if (draft.shortName.isNotBlank()) draft.shortName else draft.name.take(3).uppercase(),
                logoUrl = null,
                createdAt = "Tháng 11/2026",
                role = "Đội trưởng",
                area = if (draft.ward.isNotBlank()) draft.ward else "Hải Châu, Đà Nẵng",
                reputationScore = 100,
                personalReputationScore = 95,
                rating = 5.0,
                matchCount = 0,
                completionRate = 100,
                status = "ACTIVE",
                description = draft.description,
                skillLevels = if (draft.skillLevels.isNotEmpty()) draft.skillLevels else listOf("Vui vẻ"),
                members = listOf(
                    TeamMember("m-me", "Lê Hoàng", "Đội trưởng", "Tiền vệ trung tâm", 95, isCurrentUser = true, isDeletedUser = false),
                ),
                schedules = emptyList(),
            )
            mockTeam = created
            ApiResult.Success(created)
        }

    override suspend fun inviteTeamMember(teamId: String, name: String, position: String): ApiResult<TeamMember> =
        withContext(Dispatchers.IO) {
            val newMember = TeamMember(
                id = "m-${System.currentTimeMillis()}",
                name = name,
                role = "Thành viên",
                position = position,
                reputationScore = 90,
                isCurrentUser = false,
                isDeletedUser = false,
            )
            mockTeam = mockTeam.copy(members = mockTeam.members + newMember)
            ApiResult.Success(newMember)
        }
}
