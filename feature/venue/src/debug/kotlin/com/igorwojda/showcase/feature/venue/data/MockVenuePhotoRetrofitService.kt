@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "MagicNumber", "ReturnCount", "StringLiteralDuplication")

package com.igorwojda.showcase.feature.venue.data

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreVenuePhoto
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.SaveVenueImageRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.UploadSignatureDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueImageDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenuePhotoRetrofitService

internal class MockVenuePhotoRetrofitService(
    private val store: ArenaInMemoryStore,
) : VenuePhotoRetrofitService {
    override suspend fun requestImageUploadSignature(venueId: String): ApiResult<ApiEnvelope<UploadSignatureDto>> =
        store.executeApi("$VENUES_PATH/$venueId/image-upload-signatures") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId")

            if (venue.status == STATUS_INACTIVE && user.role != AccountRole.ADMIN) {
                return@executeApi inactiveVenueError("$VENUES_PATH/$venueId")
            }

            val currentCount = snapshot.venuePhotos.count { it.venueId == venueId }
            if (currentCount >= MAX_VENUE_PHOTOS) {
                return@executeApi store.apiError(
                    httpStatus = 422,
                    code = "VENUE_003",
                    message = "Tối đa 5 ảnh mỗi địa điểm",
                    path = "$VENUES_PATH/$venueId/image-upload-signatures",
                )
            }

            val nowSec = (VietnamDateTime.currentEpochMillis(snapshot.mockClockIso) / 1000L)
            ApiResult.Success(
                ApiEnvelope(
                    UploadSignatureDto(
                        uploadUrl = "https://api.cloudinary.com/v1_1/arenaflow-demo/image/upload",
                        cloudName = "arenaflow-demo",
                        apiKey = "123456789012345",
                        publicId = "arenaflow-demo/venues/$venueId/$nowSec-${currentCount + 1}",
                        timestamp = nowSec,
                        allowedFormats = "jpg,jpeg,png,webp",
                        overwrite = false,
                        signature = "mock-sig-$nowSec",
                        expiresAt = VietnamDateTime.nowIso(snapshot.mockClockIso),
                    ),
                ),
            )
        }

    override suspend fun saveVenueImage(
        venueId: String,
        request: SaveVenueImageRequestDto,
    ): ApiResult<ApiEnvelope<VenueImageDto>> =
        store.executeApi("$VENUES_PATH/$venueId/images") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId/images")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId/images")
            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId/images")

            if (venue.status == STATUS_INACTIVE && user.role != AccountRole.ADMIN) {
                return@executeApi inactiveVenueError("$VENUES_PATH/$venueId/images")
            }

            val existing = snapshot.venuePhotos.filter { it.venueId == venueId }
            if (existing.size >= MAX_VENUE_PHOTOS) {
                return@executeApi store.apiError(
                    httpStatus = 422,
                    code = "VENUE_003",
                    message = "Tối đa 5 ảnh mỗi địa điểm",
                    path = "$VENUES_PATH/$venueId/images",
                )
            }

            val nextOrder = (existing.maxOfOrNull { it.displayOrder } ?: 0) + 1
            val shouldBePrimary = existing.isEmpty() || request.isPrimary
            val newId = "vp-${System.currentTimeMillis()}"
            val newPhoto =
                StoreVenuePhoto(
                    id = newId,
                    venueId = venueId,
                    label = "Ảnh mặt sân #$nextOrder",
                    imageUrl = "local://$newId",
                    isPrimary = shouldBePrimary,
                    displayOrder = nextOrder,
                )

            store.update { current ->
                val updatedPhotos =
                    if (shouldBePrimary) {
                        current.venuePhotos.map { photo ->
                            if (photo.venueId == venueId) photo.copy(isPrimary = false) else photo
                        }
                    } else {
                        current.venuePhotos
                    }
                current.copy(venuePhotos = updatedPhotos + newPhoto)
            }

            ApiResult.Success(
                ApiEnvelope(
                    newPhoto.toDto(
                        uploadedById = uid,
                        createdAt = VietnamDateTime.nowIso(snapshot.mockClockIso),
                    ),
                ),
            )
        }

    override suspend fun setPrimaryImage(
        venueId: String,
        imageId: String,
    ): ApiResult<ApiEnvelope<List<VenueImageDto>>> =
        store.executeApi("$VENUES_PATH/$venueId/images/$imageId/set-primary") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId")

            if (venue.status == STATUS_INACTIVE && user.role != AccountRole.ADMIN) {
                return@executeApi inactiveVenueError("$VENUES_PATH/$venueId")
            }

            val exists = snapshot.venuePhotos.any { it.venueId == venueId && it.id == imageId }
            if (!exists) {
                return@executeApi imageNotFound("$VENUES_PATH/$venueId/images/$imageId")
            }

            store.update { current ->
                current.copy(
                    venuePhotos =
                        current.venuePhotos.map { photo ->
                            if (photo.venueId == venueId) {
                                photo.copy(isPrimary = photo.id == imageId)
                            } else {
                                photo
                            }
                        },
                )
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val result =
                store.current.venuePhotos
                    .filter { it.venueId == venueId }
                    .sortedBy { it.displayOrder }
                    .map { it.toDto(venue.createdBy, nowIso) }

            ApiResult.Success(ApiEnvelope(result))
        }

    override suspend fun deleteVenueImage(
        venueId: String,
        imageId: String,
    ): ApiResult<ApiEnvelope<List<VenueImageDto>>> =
        store.executeApi("$VENUES_PATH/$venueId/images/$imageId") { snapshot ->
            val uid = snapshot.currentUserId ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val user = snapshot.users[uid] ?: return@executeApi unauthorized("$VENUES_PATH/$venueId")
            val isAdmin = user.role == AccountRole.ADMIN
            val venue =
                snapshot.venues.firstOrNull { it.id == venueId }
                    ?: return@executeApi venueNotFound("$VENUES_PATH/$venueId")

            if (!isAdmin && venue.createdBy != uid) {
                return@executeApi store.apiError(
                    httpStatus = 403,
                    code = "VENUE_013",
                    message = "Chỉ người tạo địa điểm hoặc quản trị viên được thực hiện",
                    path = "$VENUES_PATH/$venueId/images/$imageId",
                )
            }

            if (!isAdmin && venue.status == STATUS_INACTIVE) {
                return@executeApi inactiveVenueError("$VENUES_PATH/$venueId")
            }

            val target =
                snapshot.venuePhotos.firstOrNull { it.venueId == venueId && it.id == imageId }
                    ?: return@executeApi imageNotFound("$VENUES_PATH/$venueId/images/$imageId")

            store.update { current ->
                val remaining = current.venuePhotos.filterNot { it.venueId == venueId && it.id == imageId }
                val promoted =
                    if (target.isPrimary) {
                        val firstRemainingId =
                            remaining
                                .filter { it.venueId == venueId }
                                .minWithOrNull(compareBy<StoreVenuePhoto> { it.displayOrder }.thenBy { it.id })
                                ?.id
                        remaining.map { photo ->
                            if (photo.venueId == venueId && photo.id == firstRemainingId) {
                                photo.copy(isPrimary = true)
                            } else {
                                photo
                            }
                        }
                    } else {
                        remaining
                    }
                current.copy(venuePhotos = promoted)
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val result =
                store.current.venuePhotos
                    .filter { it.venueId == venueId }
                    .sortedBy { it.displayOrder }
                    .map { it.toDto(venue.createdBy, nowIso) }

            ApiResult.Success(ApiEnvelope(result))
        }

    private fun StoreVenuePhoto.toDto(
        uploadedById: String,
        createdAt: String,
    ): VenueImageDto =
        VenueImageDto(
            imageId = id,
            venueId = venueId,
            imageUrl = imageUrl,
            isPrimary = isPrimary,
            displayOrder = displayOrder,
            uploadedById = uploadedById,
            createdAt = createdAt,
        )

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

    private fun <T> inactiveVenueError(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = 422,
            code = "VENUE_002",
            message = "Địa điểm đã ngừng hoạt động",
            path = path,
        )

    private fun <T> imageNotFound(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = 404,
            code = "VENUE_004",
            message = "Không tìm thấy ảnh",
            path = path,
        )

    private companion object {
        const val VENUES_PATH = "/api/v1/venues"
        const val STATUS_INACTIVE = "INACTIVE"
        const val MAX_VENUE_PHOTOS = 5
    }
}
