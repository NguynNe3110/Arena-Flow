package com.igorwojda.showcase.feature.venue.domain.model

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat

enum class VenueStatus(
    val label: String,
) {
    ACTIVE("Hoạt động"),
    INACTIVE("Ngừng HĐ"),
    ;

    companion object {
        fun fromApiValue(value: String?): VenueStatus = entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: ACTIVE
    }
}

enum class VenueReportReason(
    val apiValue: String,
    val label: String,
) {
    CLOSED("CLOSED", "Sân đã đóng cửa / ngừng hoạt động"),
    WRONG_INFO("WRONG_INFO", "Sai địa chỉ hoặc thông tin sân"),
    OTHER("OTHER", "Thông tin khác"),
    ;

    companion object {
        fun fromApiValue(value: String?): VenueReportReason =
            entries.firstOrNull { it.apiValue.equals(value?.trim(), ignoreCase = true) } ?: OTHER
    }
}

data class VenueWard(
    val wardId: String,
    val wardName: String,
    val provinceId: String,
    val provinceName: String,
) {
    val fullLabel: String
        get() = "$wardName, $provinceName"
}

data class VenueImage(
    val imageId: String,
    val venueId: String,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int,
    val uploadedById: String,
    val createdAt: String,
)

data class VenueSummary(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val venueType: PitchFormat?,
    val status: VenueStatus,
    val primaryImageUrl: String?,
    val distanceKm: Double?,
    val lastSelectedAt: String?,
) {
    val isInactive: Boolean
        get() = status == VenueStatus.INACTIVE
}

data class VenueDetail(
    val venueId: String,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val ward: VenueWard?,
    val venueType: PitchFormat?,
    val phone: String?,
    val website: String?,
    val status: VenueStatus,
    val createdByType: AccountRole,
    val createdById: String?,
    val createdByName: String?,
    val lastSelectedAt: String?,
    val adminVerifiedInactiveAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val images: List<VenueImage>,
) {
    val isInactive: Boolean
        get() = status == VenueStatus.INACTIVE
}

data class VenueReport(
    val reportId: String,
    val venueId: String,
    val reason: VenueReportReason,
    val detail: String?,
    val status: String,
    val createdAt: String,
)

data class VenueWardOption(
    val wardId: String,
    val wardName: String,
    val provinceId: String,
    val provinceName: String,
)

data class CreateVenueParams(
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val wardId: String,
    val venueType: PitchFormat? = null,
    val phone: String? = null,
    val website: String? = null,
)

data class UpdateVenueParams(
    val venueName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val wardId: String? = null,
    val venueType: String? = null,
    val phone: String? = null,
    val website: String? = null,
)
