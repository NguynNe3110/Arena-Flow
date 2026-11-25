package com.igorwojda.showcase.feature.admin.data.datasource.api.service

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdjustReputationRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminUserSummaryDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AuditLogDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.BanUserRequestDto
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit service for admin user sanctions and audit log endpoints. */
interface AdminUserRetrofitService {
    @GET("admin/users")
    suspend fun getUsers(
        @Query("query") query: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<AdminUserSummaryDto>>

    @POST("admin/users/{userId}/ban")
    suspend fun banUser(
        @Path("userId") userId: String,
        @Body request: BanUserRequestDto,
    ): ApiResult<Unit>

    @POST("admin/users/{userId}/unban")
    suspend fun unbanUser(
        @Path("userId") userId: String,
    ): ApiResult<Unit>

    @POST("admin/users/{userId}/adjust-reputation")
    suspend fun adjustReputation(
        @Path("userId") userId: String,
        @Body request: AdjustReputationRequestDto,
    ): ApiResult<Unit>

    @GET("admin/audit-logs")
    suspend fun getAuditLogs(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): ApiResult<PaginatedEnvelope<AuditLogDto>>
}
