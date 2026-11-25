package com.igorwojda.showcase.feature.venue.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.SaveVenueImageRequestDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.UploadSignatureDto
import com.igorwojda.showcase.feature.venue.data.datasource.api.model.VenueImageDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface VenuePhotoRetrofitService {
    @POST("venues/{venueId}/image-upload-signatures")
    suspend fun requestImageUploadSignature(
        @Path("venueId") venueId: String,
    ): ApiResult<ApiEnvelope<UploadSignatureDto>>

    @POST("venues/{venueId}/images")
    suspend fun saveVenueImage(
        @Path("venueId") venueId: String,
        @Body request: SaveVenueImageRequestDto,
    ): ApiResult<ApiEnvelope<VenueImageDto>>

    @PATCH("venues/{venueId}/images/{imageId}/set-primary")
    suspend fun setPrimaryImage(
        @Path("venueId") venueId: String,
        @Path("imageId") imageId: String,
    ): ApiResult<ApiEnvelope<List<VenueImageDto>>>

    @DELETE("venues/{venueId}/images/{imageId}")
    suspend fun deleteVenueImage(
        @Path("venueId") venueId: String,
        @Path("imageId") imageId: String,
    ): ApiResult<ApiEnvelope<List<VenueImageDto>>>
}
