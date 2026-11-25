@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "MagicNumber")

package com.igorwojda.showcase.feature.profile.data

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreUser
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.AuthProviderDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.MyTeamItemDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.ProfileResponseDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.ProfileWardDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.ProvinceItemDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.UpdateProfileRequestDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.WardItemDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.service.ProfileRetrofitService
import java.time.LocalDate
import java.time.Period
import kotlin.math.ceil

internal class MockProfileRetrofitService(
    private val store: ArenaInMemoryStore,
) : ProfileRetrofitService {
    override suspend fun getMyProfile(): ApiResult<ApiEnvelope<ProfileResponseDto>> =
        store.executeApi(PROFILE_PATH) { snapshot ->
            val userResult = resolveActiveUser<ApiEnvelope<ProfileResponseDto>>(snapshot, PROFILE_PATH)
            if (userResult.error != null) return@executeApi userResult.error
            val user = requireNotNull(userResult.user)
            ApiResult.Success(ApiEnvelope(user.toProfileDto(snapshot)))
        }

    override suspend fun updateMyProfile(request: UpdateProfileRequestDto): ApiResult<ApiEnvelope<ProfileResponseDto>> =
        store.executeApi(PROFILE_PATH) { snapshot ->
            val userResult = resolveActiveUser<ApiEnvelope<ProfileResponseDto>>(snapshot, PROFILE_PATH)
            if (userResult.error != null) return@executeApi userResult.error
            val user = requireNotNull(userResult.user)

            val validationError = validateUpdateRequest(request, snapshot)
            if (validationError != null) return@executeApi validationError

            val resolvedWardId = request.wardId ?: user.wardId
            if (request.wardId != null && snapshot.wards.none { it.id == request.wardId }) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "USER_002",
                    message = "Khu vực không hợp lệ",
                    path = PROFILE_PATH,
                )
            }

            val updatedUser =
                user.copy(
                    displayName = request.displayName?.trim() ?: user.displayName,
                    bio = request.bio?.trim() ?: user.bio,
                    preferredPosition =
                        request.preferredPosition?.let { PlayerPosition.fromApiLabel(it) }
                            ?: user.preferredPosition,
                    dateOfBirth = request.dateOfBirth ?: user.dateOfBirth,
                    gender = request.gender ?: user.gender,
                    wardId = resolvedWardId,
                    isNewUser = false,
                )

            store.update { current ->
                current.copy(users = current.users + (updatedUser.id to updatedUser))
            }

            ApiResult.Success(ApiEnvelope(updatedUser.toProfileDto(store.current)))
        }

    override suspend fun deleteAccount(): ApiResult<Unit> =
        store.executeApi(PROFILE_PATH) { snapshot ->
            val uid =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(
                        httpStatus = 401,
                        code = "AUTH_001",
                        message = "Phiên đăng nhập hết hạn.",
                        path = PROFILE_PATH,
                    )

            val user = snapshot.users[uid]
            if (user == null || user.isDeleted) {
                return@executeApi ApiResult.Success(Unit)
            }

            val hasBlockingTeams = snapshot.teams.any { it.captainId == uid && it.status != STATUS_DISBANDED }
            if (hasBlockingTeams) {
                return@executeApi store.apiError(
                    httpStatus = 422,
                    code = "USER_003",
                    message = "Bạn phải chuyển quyền đội trưởng hoặc giải tán đội trước khi xóa tài khoản.",
                    path = PROFILE_PATH,
                )
            }

            store.update { current ->
                val deletedUser = user.copy(isDeleted = true, displayName = DELETED_USER_DISPLAY_NAME)
                val updatedMembers =
                    current.teamMembers.map { member ->
                        if (member.userId == uid && member.status == STATUS_ACTIVE) {
                            member.copy(status = STATUS_LEFT, leftAt = current.mockClockIso)
                        } else {
                            member
                        }
                    }
                val updatedInvitations =
                    current.invitations.map { inv ->
                        if (inv.invitedUserId == uid && inv.status == STATUS_PENDING) {
                            inv.copy(status = STATUS_REJECTED)
                        } else {
                            inv
                        }
                    }
                val updatedApplications =
                    current.applications.map { app ->
                        if (app.applicantId == uid && (app.status == STATUS_PENDING || app.status == STATUS_APPROVED)) {
                            app.copy(status = STATUS_WITHDRAWN)
                        } else {
                            app
                        }
                    }
                current.copy(
                    currentUserId = null,
                    actingTeamId = null,
                    users = current.users + (uid to deletedUser),
                    teamMembers = updatedMembers,
                    invitations = updatedInvitations,
                    applications = updatedApplications,
                )
            }

            ApiResult.Success(Unit)
        }

    override suspend fun getProvinces(): ApiResult<ApiEnvelope<List<ProvinceItemDto>>> =
        store.executeApi(PROVINCES_PATH) { snapshot ->
            val provinces =
                snapshot.wards
                    .map { it.city }
                    .distinct()
                    .sorted()
                    .map { city -> ProvinceItemDto(provinceId = provinceIdForCity(city), provinceName = city) }
            ApiResult.Success(ApiEnvelope(provinces))
        }

    override suspend fun getWardsByProvince(provinceId: String): ApiResult<ApiEnvelope<List<WardItemDto>>> =
        store.executeApi("$PROVINCES_PATH/$provinceId/wards") { snapshot ->
            val matchingCity =
                snapshot.wards
                    .map { it.city }
                    .distinct()
                    .firstOrNull { provinceIdForCity(it) == provinceId }

            if (matchingCity == null) {
                return@executeApi store.apiError(
                    httpStatus = 404,
                    code = "USER_004",
                    message = "Không tìm thấy tỉnh/thành",
                    path = "$PROVINCES_PATH/$provinceId/wards",
                )
            }

            val wards =
                snapshot.wards
                    .filter { it.city == matchingCity }
                    .sortedBy { it.name }
                    .map { WardItemDto(wardId = it.id, wardName = it.fullLabel) }

            ApiResult.Success(ApiEnvelope(wards))
        }

    override suspend fun getMyTeams(
        role: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MyTeamItemDto>> =
        store.executeApi(PROFILE_TEAMS_PATH) { snapshot ->
            val userResult = resolveActiveUser<PaginatedEnvelope<MyTeamItemDto>>(snapshot, PROFILE_TEAMS_PATH)
            if (userResult.error != null) return@executeApi userResult.error
            val uid = requireNotNull(userResult.user).id

            val activeMemberships =
                snapshot.teamMembers
                    .filter { it.userId == uid && it.status == STATUS_ACTIVE }
                    .associateBy { it.teamId }

            val allItems =
                snapshot.teams
                    .filter { team ->
                        team.status != STATUS_DISBANDED && activeMemberships.containsKey(team.id)
                    }.map { team ->
                        val membership = activeMemberships.getValue(team.id)
                        val memberRole = if (team.captainId == uid) ROLE_CAPTAIN else ROLE_MEMBER
                        val memberCount =
                            snapshot.teamMembers.count { it.teamId == team.id && it.status == STATUS_ACTIVE }
                        MyTeamItemDto(
                            teamId = team.id,
                            teamName = team.name,
                            avatarUrl = null,
                            skillLevels = team.skillLevels.map { it.label },
                            status = team.status,
                            captainName = store.displayUserName(team.captainId),
                            memberCount = memberCount.coerceAtLeast(1),
                            role = memberRole,
                            joinedAt = membership.joinedAt,
                        )
                    }.filter { item ->
                        role == null || item.role == role
                    }.sortedWith(
                        compareByDescending<MyTeamItemDto> { it.role == ROLE_CAPTAIN }
                            .thenByDescending { it.joinedAt }
                            .thenByDescending { it.teamId },
                    )

            val safePage = page.coerceAtLeast(1)
            val safeSize = pageSize.coerceIn(1, 100)
            val fromIndex = (safePage - 1) * safeSize
            val paged =
                if (fromIndex >= allItems.size) {
                    emptyList()
                } else {
                    allItems.subList(fromIndex, (fromIndex + safeSize).coerceAtMost(allItems.size))
                }
            val totalPages = if (allItems.isEmpty()) 0 else ceil(allItems.size.toDouble() / safeSize).toInt()

            ApiResult.Success(
                PaginatedEnvelope(
                    data = paged,
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = safeSize,
                            totalPages = totalPages,
                            totalItems = allItems.size,
                        ),
                ),
            )
        }

    private fun validateUpdateRequest(
        request: UpdateProfileRequestDto,
        snapshot: ArenaStoreSnapshot,
    ): ApiResult.Error<ApiEnvelope<ProfileResponseDto>>? {
        val fieldErrors = mutableListOf<FieldValidationErrorDto>()
        request.displayName?.let { name ->
            if (name.isBlank() || name.length > MAX_DISPLAY_NAME_LENGTH) {
                fieldErrors += FieldValidationErrorDto("displayName", "Tên hiển thị không hợp lệ")
            }
        }
        request.dateOfBirth?.let { dob ->
            val parsedDob = runCatching { LocalDate.parse(dob) }.getOrNull()
            val today =
                runCatching { LocalDate.parse(snapshot.mockClockIso.take(10)) }.getOrElse { LocalDate.now() }
            if (parsedDob == null || !parsedDob.isBefore(today) || Period.between(parsedDob, today).years < MIN_AGE) {
                fieldErrors += FieldValidationErrorDto("dateOfBirth", "Bạn phải đủ 13 tuổi")
            }
        }
        if (fieldErrors.isNotEmpty()) {
            return store.apiError(
                httpStatus = 400,
                code = "VALIDATION_ERROR",
                message = "Dữ liệu không hợp lệ",
                path = PROFILE_PATH,
                validationErrors = fieldErrors,
            )
        }
        return null
    }

    private fun <T> resolveActiveUser(
        snapshot: ArenaStoreSnapshot,
        path: String,
    ): UserLookupResult<T> {
        val uid =
            snapshot.currentUserId
                ?: return UserLookupResult(
                    error =
                        store.apiError(
                            httpStatus = 401,
                            code = "AUTH_001",
                            message = "Vui lòng đăng nhập để tiếp tục.",
                            path = path,
                        ),
                )
        val user = snapshot.users[uid]
        if (user == null || user.isDeleted) {
            return UserLookupResult(
                error =
                    store.apiError(
                        httpStatus = 404,
                        code = "USER_001",
                        message = "Không tìm thấy người dùng.",
                        path = path,
                    ),
            )
        }
        return UserLookupResult(user = user)
    }

    private fun StoreUser.toProfileDto(snapshot: ArenaStoreSnapshot): ProfileResponseDto {
        val wardOption: WardOption? = snapshot.wards.firstOrNull { it.id == wardId }
        val providers =
            buildList {
                email?.let { add(AuthProviderDto(provider = "GOOGLE", providerUserId = it)) }
            }
        return ProfileResponseDto(
            userId = id,
            role = role.name,
            providers = providers,
            phoneNumber = phone.ifBlank { null },
            displayName = displayName,
            avatarUrl = null,
            bio = bio.ifBlank { null },
            preferredPosition = preferredPosition?.label,
            dateOfBirth = dateOfBirth,
            gender = gender,
            ward =
                wardOption?.let {
                    ProfileWardDto(
                        wardId = it.id,
                        wardName = it.fullLabel,
                        provinceId = provinceIdForCity(it.city),
                        provinceName = it.city,
                    )
                },
            createdAt = snapshot.mockClockIso,
            updatedAt = snapshot.mockClockIso,
        )
    }

    private fun provinceIdForCity(city: String): String =
        when (city) {
            "TP. Hồ Chí Minh" -> "province-hcm"
            "Hà Nội" -> "province-hn"
            "Đà Nẵng" -> "province-dn"
            else -> "province-${city.lowercase().replace(Regex("[^a-z0-9]+"), "-")}"
        }

    private data class UserLookupResult<T>(
        val user: StoreUser? = null,
        val error: ApiResult.Error<T>? = null,
    )

    private companion object {
        const val PROFILE_PATH = "/api/v1/profile"
        const val PROVINCES_PATH = "/api/v1/provinces"
        const val PROFILE_TEAMS_PATH = "/api/v1/profile/teams"
        const val DELETED_USER_DISPLAY_NAME = "Người dùng đã xóa"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_DISBANDED = "DISBANDED"
        const val STATUS_LEFT = "LEFT"
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
        const val STATUS_WITHDRAWN = "WITHDRAWN"
        const val ROLE_CAPTAIN = "CAPTAIN"
        const val ROLE_MEMBER = "MEMBER"
        const val MAX_DISPLAY_NAME_LENGTH = 100
        const val MIN_AGE = 13
    }
}
