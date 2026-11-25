package com.igorwojda.showcase.feature.venue.data.datasource.api.model

import kotlinx.serialization.Serializable

@Serializable
data class VenueWardDto(
    val wardId: String,
    val wardName: String,
    val provinceId: String,
    val provinceName: String,
)

@Serializable
data class VenueImageDto(
    val imageId: String,
    val venueId: String,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int,
    val uploadedById: String,
    val createdAt: String,
)

@Serializable
data class VenueDto(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val ward: VenueWardDto? = null,
    val venueType: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val status: String,
    val createdByType: String,
    val createdById: String? = null,
    val createdByName: String? = null,
    val lastSelectedAt: String? = null,
    val adminVerifiedInactiveAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val images: List<VenueImageDto> = emptyList(),
)

@Serializable
data class VenueSummaryDto(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val venueType: String? = null,
    val status: String,
    val primaryImageUrl: String? = null,
    val distanceKm: Double? = null,
    val lastSelectedAt: String? = null,
)

@Serializable
data class CreateVenueRequestDto(
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val wardId: String,
    val venueType: String? = null,
    val phone: String? = null,
    val website: String? = null,
)

@Serializable
data class UpdateVenueRequestDto(
    val venueName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val wardId: String? = null,
    val venueType: String? = null,
    val phone: String? = null,
    val website: String? = null,
)

@Serializable
data class UploadSignatureDto(
    val uploadUrl: String,
    val cloudName: String,
    val apiKey: String,
    val publicId: String,
    val timestamp: Long,
    val allowedFormats: String,
    val overwrite: Boolean,
    val signature: String,
    val expiresAt: String,
)

@Serializable
data class SaveVenueImageRequestDto(
    val publicId: String,
    val version: Long,
    val format: String,
    val signature: String,
    val isPrimary: Boolean = false,
)

@Serializable
data class ReportVenueRequestDto(
    val reason: String,
    val detail: String? = null,
)

@Serializable
data class VenueReportDto(
    val reportId: String,
    val venueId: String,
    val reason: String,
    val detail: String? = null,
    val status: String,
    val createdAt: String,
)

@Serializable
data class ProvinceItemDto(
    val provinceId: String,
    val provinceName: String,
)

@Serializable
data class WardItemDto(
    val wardId: String,
    val wardName: String,
)
