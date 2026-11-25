package com.igorwojda.showcase.feature.mercenary.data.datasource.api.service

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Read-only Retrofit service for mercenary recruitment queries. */
interface MercenaryQueryRetrofitService {
    @Suppress("LongParameterList")
    @GET("mercenary-posts")
    suspend fun getMercenaryPosts(
        @Query("wardId") wardId: String? = null,
        @Query("position") position: String? = null,
        @Query("format") format: String? = null,
        @Query("skillLevel") skillLevel: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MercenaryPostSummaryDto>>

    @GET("mercenary-posts/my")
    suspend fun getMyTeamPosts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<MercenaryPostSummaryDto>>

    @GET("mercenary-posts/{postId}")
    suspend fun getMercenaryPostDetail(
        @Path("postId") postId: String,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>>

    @GET("mercenary-posts/{postId}/applications")
    suspend fun getPostApplications(
        @Path("postId") postId: String,
    ): ApiResult<ApiEnvelope<List<MercenaryApplicationDto>>>

    @GET("mercenary-applications/my")
    suspend fun getMyApplications(): ApiResult<ApiEnvelope<List<MercenaryApplicationDto>>>
}
