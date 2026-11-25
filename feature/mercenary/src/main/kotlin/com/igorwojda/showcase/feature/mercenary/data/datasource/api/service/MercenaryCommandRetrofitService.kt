package com.igorwojda.showcase.feature.mercenary.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.ApplyMercenaryRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.CreateMercenaryPostRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.UpdateMercenaryPostRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Write Retrofit service for mercenary recruitment mutations. */
interface MercenaryCommandRetrofitService {
    @POST("mercenary-posts")
    suspend fun createMercenaryPost(
        @Body request: CreateMercenaryPostRequestDto,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>>

    @PUT("mercenary-posts/{postId}")
    suspend fun updateMercenaryPost(
        @Path("postId") postId: String,
        @Body request: UpdateMercenaryPostRequestDto,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>>

    @POST("mercenary-posts/{postId}/close")
    suspend fun closeMercenaryPost(
        @Path("postId") postId: String,
    ): ApiResult<Unit>

    @POST("mercenary-posts/{postId}/apply")
    suspend fun applyToMercenaryPost(
        @Path("postId") postId: String,
        @Body request: ApplyMercenaryRequestDto,
    ): ApiResult<ApiEnvelope<MercenaryApplicationDto>>

    @POST("mercenary-posts/{postId}/applications/{appId}/accept")
    suspend fun acceptApplication(
        @Path("postId") postId: String,
        @Path("appId") appId: String,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>>

    @POST("mercenary-posts/{postId}/applications/{appId}/reject")
    suspend fun rejectApplication(
        @Path("postId") postId: String,
        @Path("appId") appId: String,
    ): ApiResult<Unit>

    @POST("mercenary-posts/{postId}/applications/{appId}/withdraw")
    suspend fun withdrawApplication(
        @Path("postId") postId: String,
        @Path("appId") appId: String,
    ): ApiResult<Unit>
}
