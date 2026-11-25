package com.igorwojda.showcase.feature.venue.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueImageDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueReportDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueSummaryDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueWardDto
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWard

internal inline fun <T, R> ApiResult<T>.toDomainResult(transform: (T) -> R): Result<R> =
    when (this) {
        is ApiResult.Success -> Result.Success(transform(data))
        is ApiResult.Error -> Result.Failure(apiErrorToAppError(code, errorBody))
        is ApiResult.Exception -> Result.Failure(AppError.Network)
    }

internal fun VenueWardDto.toDomain(): VenueWard =
    VenueWard(
        wardId = wardId,
        wardName = wardName,
        provinceId = provinceId,
        provinceName = provinceName,
    )

internal fun VenueImageDto.toDomain(): VenueImage =
    VenueImage(
        imageId = imageId,
        venueId = venueId,
        imageUrl = imageUrl,
        isPrimary = isPrimary,
        displayOrder = displayOrder,
        uploadedById = uploadedById,
        createdAt = createdAt,
    )

internal fun VenueSummaryDto.toDomain(): VenueSummary =
    VenueSummary(
        venueId = venueId,
        venueName = venueName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        venueType = PitchFormat.fromApiName(venueType),
        status = VenueStatus.fromApiValue(status),
        primaryImageUrl = primaryImageUrl,
        distanceKm = distanceKm,
        lastSelectedAt = lastSelectedAt,
    )

internal fun VenueDto.toDomain(): VenueDetail =
    VenueDetail(
        venueId = venueId,
        venueName = venueName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        ward = ward?.toDomain(),
        venueType = PitchFormat.fromApiName(venueType),
        phone = phone,
        website = website,
        status = VenueStatus.fromApiValue(status),
        createdByType = if (createdByType.equals("ADMIN", ignoreCase = true)) AccountRole.ADMIN else AccountRole.USER,
        createdById = createdById,
        createdByName = createdByName,
        lastSelectedAt = lastSelectedAt,
        adminVerifiedInactiveAt = adminVerifiedInactiveAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images.sortedBy { it.displayOrder }.map { it.toDomain() },
    )

internal fun VenueReportDto.toDomain(): VenueReport =
    VenueReport(
        reportId = reportId,
        venueId = venueId,
        reason = VenueReportReason.fromApiValue(reason),
        detail = detail,
        status = status,
        createdAt = createdAt,
    )
