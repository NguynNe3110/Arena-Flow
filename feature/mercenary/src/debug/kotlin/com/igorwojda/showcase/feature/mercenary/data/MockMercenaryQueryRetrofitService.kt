@file:Suppress("LabeledExpression")

package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostSummaryDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryQueryRetrofitService

/** Debug mock implementation of [MercenaryQueryRetrofitService] backed by [ArenaInMemoryStore]. */
internal class MockMercenaryQueryRetrofitService(
    private val store: ArenaInMemoryStore,
) : MercenaryQueryRetrofitService {
    override suspend fun getMercenaryPosts(
        wardId: String?,
        position: String?,
        format: String?,
        skillLevel: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MercenaryPostSummaryDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts") { snapshot ->
            val filtered =
                snapshot.recruitments
                    .filter { rec ->
                        val team = snapshot.teams.firstOrNull { it.id == rec.teamId }
                        val teamActive = team == null || team.status == "ACTIVE"
                        val venue = snapshot.venues.firstOrNull { it.id == rec.venueId }
                        val match = snapshot.matches.firstOrNull { it.id == rec.matchId }
                        val recFormat = (match?.format ?: venue?.type)?.name
                        val matchesWard = wardId.isNullOrBlank() || rec.wardId == wardId
                        val matchesPos =
                            position.isNullOrBlank() ||
                                rec.position.label.equals(position, ignoreCase = true) ||
                                rec.position.name.equals(position, ignoreCase = true)
                        val matchesFormat = format.isNullOrBlank() || recFormat.equals(format, ignoreCase = true)
                        val matchesSkill =
                            skillLevel.isNullOrBlank() ||
                                rec.skillLevel.label.equals(skillLevel, ignoreCase = true) ||
                                rec.skillLevel.name.equals(skillLevel, ignoreCase = true)
                        rec.status == "OPEN" && teamActive && matchesWard && matchesPos && matchesFormat && matchesSkill
                    }.map { MockMercenaryMapper.toSummaryDto(store, snapshot, it) }

            val (paged, safePage, totalPages) = MockMercenaryMapper.paginate(filtered, page, pageSize)
            ApiResult.Success(
                PaginatedEnvelope(
                    data = paged,
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = pageSize,
                            totalPages = totalPages,
                            totalItems = filtered.size,
                        ),
                ),
            )
        }

    override suspend fun getMyTeamPosts(
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MercenaryPostSummaryDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts/my") { snapshot ->
            val currentUserId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val captainedTeamIds =
                snapshot.teams
                    .filter { it.captainId == currentUserId }
                    .map { it.id }
                    .toSet()
            val myPosts =
                snapshot.recruitments
                    .filter { it.teamId in captainedTeamIds }
                    .map { MockMercenaryMapper.toSummaryDto(store, snapshot, it) }
            val (paged, safePage, totalPages) = MockMercenaryMapper.paginate(myPosts, page, pageSize)
            ApiResult.Success(
                PaginatedEnvelope(
                    data = paged,
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = pageSize,
                            totalPages = totalPages,
                            totalItems = myPosts.size,
                        ),
                ),
            )
        }

    override suspend fun getMercenaryPostDetail(postId: String): ApiResult<ApiEnvelope<MercenaryPostDetailDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId") { snapshot ->
            val recruitment =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            ApiResult.Success(ApiEnvelope(MockMercenaryMapper.toDetailDto(store, snapshot, recruitment)))
        }

    override suspend fun getPostApplications(postId: String): ApiResult<ApiEnvelope<List<MercenaryApplicationDto>>> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/applications") { snapshot ->
            val recruitment =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            val team = snapshot.teams.firstOrNull { it.id == recruitment.teamId }
            if (team?.captainId != snapshot.currentUserId) {
                return@executeApi store.apiError(
                    HTTP_FORBIDDEN,
                    "TEAM_002",
                    "Chỉ đội trưởng mới xem được danh sách ứng viên.",
                )
            }
            val apps =
                snapshot.applications
                    .filter { it.recruitmentId == postId }
                    .map { MockMercenaryMapper.toApplicationDto(store, snapshot, it, recruitment) }
            ApiResult.Success(ApiEnvelope(apps))
        }

    override suspend fun getMyApplications(): ApiResult<ApiEnvelope<List<MercenaryApplicationDto>>> =
        store.executeApi(path = "/api/v1/mercenary-applications/my") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val apps =
                snapshot.applications
                    .filter { it.applicantId == userId }
                    .map { MockMercenaryMapper.toApplicationDto(store, snapshot, it) }
            ApiResult.Success(ApiEnvelope(apps))
        }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
    }
}
