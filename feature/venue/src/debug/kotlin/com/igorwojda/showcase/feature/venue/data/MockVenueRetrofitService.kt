@file:Suppress(
    "CognitiveComplexMethod",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "MagicNumber",
    "ReturnCount",
    "StringLiteralDuplication",
    "TooManyFunctions",
)

package com.igorwojda.showcase.feature.venue.data

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreReport
import com.igorwojda.showcase.feature.base.data.store.StoreVenue
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.CreateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.ProvinceItemDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.ReportVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.UpdateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueImageDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueReportDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueSummaryDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueWardDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.WardItemDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenueRetrofitService
import kotlin.math.ceil

internal class MockVenueRetrofitService(
    private val store: ArenaInMemoryStore,
) : VenueRetrofitService {
    override suspend fun getVenues(
        q: String?,
        lat: Double?,
        lng: Double?,
        radiusKm: Double?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<VenueSummaryDto>> =
        store.executeApi(VENUES_PATH) { snapshot ->
            val trimmed = q?.trim()?.lowercase().orEmpty()
            val filtered =
                snapshot.venues
                    .filter { venue ->
                        if (trimmed.isNotEmpty()) {
                            venue.name.lowercase().contains(trimmed) || venue.address.lowercase().contains(trimmed)
                        } else {
                            true
                        }
                    }.sortedWith(
                        compareBy<StoreVenue> { it.status == STATUS_INACTIVE }
                            .thenBy { it.distanceKm }
                            .thenBy { it.name },
                    )

            val safePageSize = pageSize.coerceIn(1, 100)
            val safePage = page.coerceAtLeast(1)
            val fromIndex = ((safePage - 1) * safePageSize).coerceAtMost(filtered.size)
            val toIndex = (fromIndex + safePageSize).coerceAtMost(filtered.size)
            val totalPages = if (filtered.isEmpty()) 1 else ceil(filtered.size / safePageSize.toDouble()).toInt()

            val items =
                filtered.subList(fromIndex, toIndex).map { venue ->
                    val photos = snapshot.venuePhotos.filter { it.venueId == venue.id }.sortedBy { it.displayOrder }
                    val primaryUrl = (photos.firstOrNull { it.isPrimary } ?: photos.firstOrNull())?.imageUrl
                    VenueSummaryDto(
                        venueId = venue.id,
                        venueName = venue.name,
                        address = venue.address,
                        latitude = venue.lat,
                        longitude = venue.lng,
                        venueType = venue.type.name,
                        status = venue.status,
                        primaryImageUrl = primaryUrl,
                        distanceKm = venue.distanceKm,
                        lastSelectedAt = null,
                    )
                }

            ApiResult.Success(
                PaginatedEnvelope(
                    data = items,
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = safePageSize,
                            totalPages = totalPages,
                            totalItems = filtered.size,
                        ),
                ),
            )
        }

    override suspend fun createVenue(request: CreateVenueRequestDto): ApiResult<ApiEnvelope<VenueDto>> =
        store.executeApi(VENUES_PATH) { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized(VENUES_PATH)
            val user = snapshot.users[uid] ?: return@executeApi unauthorized(VENUES_PATH)

            val isAdmin = user.role == AccountRole.ADMIN
            if (!isAdmin && (request.phone != null || request.website != null)) {
                return@executeApi store.apiError(
                    httpStatus = 403,
                    code = "VENUE_017",
                    message = "Chỉ quản trị viên được nhập số điện thoại và website của địa điểm",
                    path = VENUES_PATH,
                )
            }

            val errors = mutableListOf<FieldValidationErrorDto>()
            if (request.venueName.isBlank()) {
                errors += FieldValidationErrorDto("venueName", "Tên sân không được để trống")
            }
            if (request.address.isBlank()) {
                errors += FieldValidationErrorDto("address", "Địa chỉ không được để trống")
            }
            if (errors.isNotEmpty()) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = errors.first().message,
                    path = VENUES_PATH,
                    validationErrors = errors,
                )
            }

            if (snapshot.wards.none { it.id == request.wardId }) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "USER_002",
                    message = "Khu vực không hợp lệ",
                    path = VENUES_PATH,
                )
            }

            val newVenue =
                StoreVenue(
                    id = "ven-${System.currentTimeMillis()}",
                    name = request.venueName.trim(),
                    address = request.address.trim(),
                    wardId = request.wardId,
                    lat = request.latitude,
                    lng = request.longitude,
                    distanceKm = 1.5,
                    type = PitchFormat.fromApiName(request.venueType) ?: PitchFormat.SEVEN_A_SIDE,
                    contactPhone = if (isAdmin) request.phone?.trim()?.takeIf { it.isNotEmpty() } else null,
                    website = if (isAdmin) request.website?.trim()?.takeIf { it.isNotEmpty() } else null,
                    status = STATUS_ACTIVE,
                    createdBy = uid,
                )

            store.update { current ->
                current.copy(venues = listOf(newVenue) + current.venues)
            }

            ApiResult.Success(ApiEnvelope(newVenue.toVenueDto(store.current)))
        }

    override suspend fun getVenueDetail(venueId: String): ApiResult<ApiEnvelope<VenueDto>> =
        store.executeApi("$VENUES_PATH/$venueId") { snapshot ->
            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId")
            ApiResult.Success(ApiEnvelope(venue.toVenueDto(snapshot)))
        }

    override suspend fun updateVenue(
        venueId: String,
        request: UpdateVenueRequestDto,
    ): ApiResult<ApiEnvelope<VenueDto>> =
        store.executeApi("$VENUES_PATH/$venueId") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val isAdmin = user.role == AccountRole.ADMIN

            if (!isAdmin && (request.phone != null || request.website != null)) {
                return@executeApi store.apiError(
                    httpStatus = 403,
                    code = "VENUE_017",
                    message = "Chỉ quản trị viên được nhập số điện thoại và website của địa điểm",
                    path = "$VENUES_PATH/$venueId",
                )
            }

            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId")

            if (!isAdmin && venue.createdBy != uid) {
                return@executeApi store.apiError(
                    httpStatus = 403,
                    code = "VENUE_013",
                    message = "Chỉ người tạo địa điểm hoặc quản trị viên được thực hiện",
                    path = "$VENUES_PATH/$venueId",
                )
            }

            if (!isAdmin && venue.status == STATUS_INACTIVE) {
                return@executeApi store.apiError(
                    httpStatus = 422,
                    code = "VENUE_002",
                    message = "Địa điểm đã ngừng hoạt động",
                    path = "$VENUES_PATH/$venueId",
                )
            }

            val updatedWardId = request.wardId ?: venue.wardId
            if (request.wardId != null && snapshot.wards.none { it.id == request.wardId }) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "USER_002",
                    message = "Khu vực không hợp lệ",
                    path = "$VENUES_PATH/$venueId",
                )
            }

            val updatedVenue =
                venue.copy(
                    name = request.venueName?.trim()?.takeIf { it.isNotEmpty() } ?: venue.name,
                    address = request.address?.trim()?.takeIf { it.isNotEmpty() } ?: venue.address,
                    wardId = updatedWardId,
                    lat = request.latitude ?: venue.lat,
                    lng = request.longitude ?: venue.lng,
                    type = request.venueType?.let { PitchFormat.fromApiName(it) } ?: venue.type,
                    contactPhone =
                        when {
                            !isAdmin || request.phone == null -> venue.contactPhone
                            request.phone.isEmpty() -> null
                            else -> request.phone.trim()
                        },
                    website =
                        when {
                            !isAdmin || request.website == null -> venue.website
                            request.website.isEmpty() -> null
                            else -> request.website.trim()
                        },
                )

            store.update { current ->
                current.copy(venues = current.venues.map { if (it.id == venueId) updatedVenue else it })
            }

            ApiResult.Success(ApiEnvelope(updatedVenue.toVenueDto(store.current)))
        }

    override suspend fun reportVenue(
        venueId: String,
        request: ReportVenueRequestDto,
    ): ApiResult<ApiEnvelope<VenueReportDto>> =
        store.executeApi("$VENUES_PATH/$venueId/reports") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId/reports")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId/reports")
            if (user.role == AccountRole.ADMIN) {
                return@executeApi store.apiError(
                    httpStatus = 403,
                    code = "VENUE_015",
                    message = "Quản trị viên không report địa điểm; hãy sửa hoặc đánh dấu ngừng hoạt động trực tiếp",
                    path = "$VENUES_PATH/$venueId/reports",
                )
            }

            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId/reports")

            if (venue.status == STATUS_INACTIVE) {
                return@executeApi store.apiError(
                    httpStatus = 422,
                    code = "VENUE_002",
                    message = "Địa điểm đã ngừng hoạt động",
                    path = "$VENUES_PATH/$venueId/reports",
                )
            }

            val duplicatePending =
                snapshot.reports.any {
                    it.reporterType == "VENUE" &&
                        it.reporterId == uid &&
                        it.reportedId == venueId &&
                        it.type == request.reason &&
                        it.status == STATUS_PENDING
                }
            if (duplicatePending) {
                return@executeApi store.apiError(
                    httpStatus = 409,
                    code = "VENUE_014",
                    message = "Bạn đã report địa điểm này, đang chờ admin xử lý",
                    path = "$VENUES_PATH/$venueId/reports",
                )
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newReport =
                StoreReport(
                    id = "rep-ven-${System.currentTimeMillis()}",
                    reporterType = "VENUE",
                    reporterId = uid,
                    reportedId = venueId,
                    type = request.reason,
                    reason = request.detail?.trim().orEmpty(),
                    status = STATUS_PENDING,
                    createdAt = nowIso,
                )

            store.update { current ->
                current.copy(
                    reports = listOf(newReport) + current.reports,
                    venues =
                        current.venues.map {
                            if (it.id == venueId) it.copy(reportCount = it.reportCount + 1) else it
                        },
                )
            }

            ApiResult.Success(
                ApiEnvelope(
                    VenueReportDto(
                        reportId = newReport.id,
                        venueId = venueId,
                        reason = request.reason,
                        detail = request.detail?.trim()?.takeIf { it.isNotEmpty() },
                        status = STATUS_PENDING,
                        createdAt = nowIso,
                    ),
                ),
            )
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
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "USER_004",
                        message = "Không tìm thấy tỉnh/thành",
                        path = "$PROVINCES_PATH/$provinceId/wards",
                    )

            val wards =
                snapshot.wards
                    .filter { it.city == matchingCity }
                    .sortedBy { it.name }
                    .map { WardItemDto(wardId = it.id, wardName = it.fullLabel) }

            ApiResult.Success(ApiEnvelope(wards))
        }

    private fun StoreVenue.toVenueDto(snapshot: ArenaStoreSnapshot): VenueDto {
        val wardOption = snapshot.wards.firstOrNull { it.id == wardId }
        val creator = snapshot.users[createdBy]
        val photos =
            snapshot.venuePhotos
                .filter { it.venueId == id }
                .sortedBy { it.displayOrder }
                .map { photo ->
                    VenueImageDto(
                        imageId = photo.id,
                        venueId = photo.venueId,
                        imageUrl = photo.imageUrl,
                        isPrimary = photo.isPrimary,
                        displayOrder = photo.displayOrder,
                        uploadedById = createdBy,
                        createdAt = snapshot.mockClockIso,
                    )
                }
        val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
        return VenueDto(
            venueId = id,
            venueName = name,
            address = address,
            latitude = lat,
            longitude = lng,
            ward =
                wardOption?.let {
                    VenueWardDto(
                        wardId = it.id,
                        wardName = it.name,
                        provinceId = provinceIdForCity(it.city),
                        provinceName = it.city,
                    )
                },
            venueType = type.name,
            phone = contactPhone,
            website = website,
            status = status,
            createdByType = if (creator?.role == AccountRole.ADMIN) "ADMIN" else "USER",
            createdById = createdBy,
            createdByName = store.displayUserName(createdBy),
            lastSelectedAt = null,
            adminVerifiedInactiveAt = if (status == STATUS_INACTIVE) nowIso else null,
            createdAt = nowIso,
            updatedAt = nowIso,
            images = photos,
        )
    }

    private fun <T> unauthorized(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = 401,
            code = "AUTH_001",
            message = "Vui lòng đăng nhập để tiếp tục.",
            path = path,
        )

    private fun <T> venueNotFound(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = 404,
            code = "VENUE_001",
            message = "Không tìm thấy địa điểm",
            path = path,
        )

    private fun provinceIdForCity(city: String): String = "prov-${city.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')}"

    private companion object {
        const val VENUES_PATH = "/api/v1/venues"
        const val PROVINCES_PATH = "/api/v1/provinces"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
        const val STATUS_PENDING = "PENDING"
    }
}
