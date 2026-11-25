@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.venue.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.CreateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.UpdateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption

interface VenueRepository {
    suspend fun getVenues(
        query: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<VenueSummary>>

    suspend fun getVenueDetail(venueId: String): Result<VenueDetail>

    suspend fun createVenue(params: CreateVenueParams): Result<VenueDetail>

    suspend fun updateVenue(
        venueId: String,
        params: UpdateVenueParams,
    ): Result<VenueDetail>

    suspend fun reportVenue(
        venueId: String,
        reason: VenueReportReason,
        detail: String?,
    ): Result<VenueReport>

    suspend fun uploadVenueImage(
        venueId: String,
        imageLabel: String,
        isPrimary: Boolean = false,
    ): Result<VenueImage>

    suspend fun setPrimaryImage(
        venueId: String,
        imageId: String,
    ): Result<List<VenueImage>>

    suspend fun deleteVenueImage(
        venueId: String,
        imageId: String,
    ): Result<List<VenueImage>>

    suspend fun getWards(): Result<List<VenueWardOption>>
}
