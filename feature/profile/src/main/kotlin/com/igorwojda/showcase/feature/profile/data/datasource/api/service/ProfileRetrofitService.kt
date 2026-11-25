package com.igorwojda.showcase.feature.profile.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.MyTeamItemDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.ProfileResponseDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.ProvinceItemDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.UpdateProfileRequestDto
import com.igorwojda.showcase.feature.profile.data.datasource.api.model.WardItemDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileRetrofitService {
    @GET("profile")
    suspend fun getMyProfile(): ApiResult<ApiEnvelope<ProfileResponseDto>>

    @PATCH("profile")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequestDto,
    ): ApiResult<ApiEnvelope<ProfileResponseDto>>

    @DELETE("profile")
    suspend fun deleteAccount(): ApiResult<Unit>

    @GET("provinces")
    suspend fun getProvinces(): ApiResult<ApiEnvelope<List<ProvinceItemDto>>>

    @GET("provinces/{provinceId}/wards")
    suspend fun getWardsByProvince(
        @Path("provinceId") provinceId: String,
    ): ApiResult<ApiEnvelope<List<WardItemDto>>>

    @GET("profile/teams")
    suspend fun getMyTeams(
        @Query("role") role: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MyTeamItemDto>>
}
