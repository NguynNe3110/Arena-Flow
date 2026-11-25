@file:Suppress("LabeledExpression")

package com.igorwojda.showcase.feature.venue.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.CreateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.ReportVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.SaveVenueImageRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.UpdateVenueRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenuePhotoRetrofitService
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenueRetrofitService
import com.igorwojda.showcase.feature.venue.domain.model.CreateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.UpdateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import kotlinx.coroutines.flow.first

internal class VenueRepositoryImpl(
    private val sessionRepository: SessionRepository,
    private val venuePhotoRetrofitService: VenuePhotoRetrofitService,
    private val venueRetrofitService: VenueRetrofitService,
) : VenueRepository {
    override suspend fun getVenues(
        query: String?,
        lat: Double?,
        lng: Double?,
        radiusKm: Double?,
        page: Int,
        pageSize: Int,
    ): Result<List<VenueSummary>> =
        dataResult {
            venueRetrofitService
                .getVenues(
                    q = query?.trim()?.takeIf { it.isNotEmpty() },
                    lat = lat,
                    lng = lng,
                    radiusKm = radiusKm,
                    page = page,
                    pageSize = pageSize,
                ).toDomainResult { envelope ->
                    envelope.data.map { it.toDomain() }
                }
        }

    override suspend fun getVenueDetail(venueId: String): Result<VenueDetail> =
        dataResult {
            venueRetrofitService.getVenueDetail(venueId).toDomainResult { it.data.toDomain() }
        }

    override suspend fun createVenue(params: CreateVenueParams): Result<VenueDetail> =
        dataResult {
            val isAdmin = isCurrentUserAdmin()
            val request =
                CreateVenueRequestDto(
                    venueName = params.venueName.trim(),
                    address = params.address.trim(),
                    latitude = params.latitude,
                    longitude = params.longitude,
                    wardId = params.wardId,
                    venueType = params.venueType?.name,
                    phone = if (isAdmin) params.phone?.trim()?.takeIf { it.isNotEmpty() } else null,
                    website = if (isAdmin) params.website?.trim()?.takeIf { it.isNotEmpty() } else null,
                )
            venueRetrofitService.createVenue(request).toDomainResult { it.data.toDomain() }
        }

    override suspend fun updateVenue(
        venueId: String,
        params: UpdateVenueParams,
    ): Result<VenueDetail> =
        dataResult {
            val isAdmin = isCurrentUserAdmin()
            val request =
                UpdateVenueRequestDto(
                    venueName = params.venueName?.trim(),
                    address = params.address?.trim(),
                    latitude = params.latitude,
                    longitude = params.longitude,
                    wardId = params.wardId,
                    venueType = params.venueType,
                    phone = if (isAdmin) params.phone?.trim() else null,
                    website = if (isAdmin) params.website?.trim() else null,
                )
            venueRetrofitService.updateVenue(venueId, request).toDomainResult { it.data.toDomain() }
        }

    override suspend fun reportVenue(
        venueId: String,
        reason: VenueReportReason,
        detail: String?,
    ): Result<VenueReport> =
        dataResult {
            val request =
                ReportVenueRequestDto(
                    reason = reason.apiValue,
                    detail = detail?.trim()?.takeIf { it.isNotEmpty() },
                )
            venueRetrofitService.reportVenue(venueId, request).toDomainResult { it.data.toDomain() }
        }

    override suspend fun uploadVenueImage(
        venueId: String,
        imageLabel: String,
        isPrimary: Boolean,
    ): Result<VenueImage> =
        dataResult {
            when (val sigResult = venuePhotoRetrofitService.requestImageUploadSignature(venueId)) {
                is ApiResult.Success -> {
                    val signature = sigResult.data.data
                    val saveRequest =
                        SaveVenueImageRequestDto(
                            publicId = signature.publicId,
                            version = signature.timestamp,
                            format = DEFAULT_IMAGE_FORMAT,
                            signature = signature.signature,
                            isPrimary = isPrimary,
                        )
                    venuePhotoRetrofitService.saveVenueImage(venueId, saveRequest).toDomainResult {
                        it.data.toDomain()
                    }
                }

                is ApiResult.Error -> {
                    sigResult.toDomainResult { error("Unreachable") }
                }

                is ApiResult.Exception -> {
                    sigResult.toDomainResult { error("Unreachable") }
                }
            }
        }

    override suspend fun setPrimaryImage(
        venueId: String,
        imageId: String,
    ): Result<List<VenueImage>> =
        dataResult {
            venuePhotoRetrofitService.setPrimaryImage(venueId, imageId).toDomainResult { envelope ->
                envelope.data.sortedBy { it.displayOrder }.map { it.toDomain() }
            }
        }

    override suspend fun deleteVenueImage(
        venueId: String,
        imageId: String,
    ): Result<List<VenueImage>> =
        dataResult {
            venuePhotoRetrofitService.deleteVenueImage(venueId, imageId).toDomainResult { envelope ->
                envelope.data.sortedBy { it.displayOrder }.map { it.toDomain() }
            }
        }

    override suspend fun getWards(): Result<List<VenueWardOption>> =
        dataResult {
            val provincesResult = venueRetrofitService.getProvinces()
            if (provincesResult !is ApiResult.Success) {
                return@dataResult provincesResult.toDomainResult { emptyList() }
            }
            val options = mutableListOf<VenueWardOption>()
            provincesResult.data.data.forEach { province ->
                val wardsResult = venueRetrofitService.getWardsByProvince(province.provinceId)
                if (wardsResult is ApiResult.Success) {
                    wardsResult.data.data.forEach { ward ->
                        options +=
                            VenueWardOption(
                                wardId = ward.wardId,
                                wardName = ward.wardName,
                                provinceId = province.provinceId,
                                provinceName = province.provinceName,
                            )
                    }
                }
            }
            Result.Success(options)
        }

    private suspend fun isCurrentUserAdmin(): Boolean =
        sessionRepository
            .observeSession()
            .first()
            .currentUser
            ?.role == AccountRole.ADMIN

    private companion object {
        const val DEFAULT_IMAGE_FORMAT = "jpg"
    }
}
