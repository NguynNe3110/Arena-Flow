@file:Suppress("TooManyFunctions")

package com.igorwojda.showcase.feature.admin.data.repository

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdjustReputationRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.BanUserRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.RejectVenueRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.ResolveReportRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminModerationRetrofitService
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminUserRetrofitService
import com.igorwojda.showcase.feature.admin.data.mapper.toAdminPage
import com.igorwojda.showcase.feature.admin.data.mapper.toDomain
import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result

internal class AdminRepositoryImpl(
    private val moderationService: AdminModerationRetrofitService,
    private val userService: AdminUserRetrofitService,
) : AdminRepository {
    override suspend fun getReports(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<AdminPage<AdminReport>> =
        executeApi { moderationService.getReports(status = status, page = page, pageSize = pageSize) }
            .map { envelope -> envelope.toAdminPage { it.toDomain() } }

    override suspend fun resolveReport(
        reportId: String,
        decision: ReportDecision,
        note: String?,
    ): Result<AdminReport> =
        executeApi {
            moderationService.resolveReport(
                reportId = reportId,
                request = ResolveReportRequestDto(decision = decision.apiValue, note = note),
            )
        }.map { it.data.toDomain() }

    override suspend fun getPendingVenues(): Result<List<AdminPendingVenue>> =
        executeApi { moderationService.getPendingVenues() }
            .map { envelope -> envelope.data.map { it.toDomain() } }

    override suspend fun approveVenue(venueId: String): Result<Unit> = executeApi { moderationService.approveVenue(venueId = venueId) }

    override suspend fun rejectVenue(
        venueId: String,
        reason: String,
    ): Result<Unit> =
        executeApi {
            moderationService.rejectVenue(
                venueId = venueId,
                request = RejectVenueRequestDto(reason = reason),
            )
        }

    override suspend fun getUsers(
        query: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<AdminPage<AdminUserSummary>> =
        executeApi { userService.getUsers(query = query, status = status, page = page, pageSize = pageSize) }
            .map { envelope -> envelope.toAdminPage { it.toDomain() } }

    override suspend fun banUser(
        userId: String,
        reason: String,
    ): Result<Unit> =
        executeApi {
            userService.banUser(
                userId = userId,
                request = BanUserRequestDto(reason = reason),
            )
        }

    override suspend fun unbanUser(userId: String): Result<Unit> = executeApi { userService.unbanUser(userId = userId) }

    override suspend fun adjustReputation(
        userId: String,
        delta: Int,
        reason: String,
    ): Result<Unit> =
        executeApi {
            userService.adjustReputation(
                userId = userId,
                request = AdjustReputationRequestDto(delta = delta, reason = reason),
            )
        }

    override suspend fun getAuditLogs(
        page: Int,
        pageSize: Int,
    ): Result<AdminPage<AuditLogEntry>> =
        executeApi { userService.getAuditLogs(page = page, pageSize = pageSize) }
            .map { envelope -> envelope.toAdminPage { it.toDomain() } }

    private suspend fun <T> executeApi(call: suspend () -> ApiResult<T>): Result<T> =
        dataResult {
            when (val result = call()) {
                is ApiResult.Success -> {
                    Result.Success(result.data)
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(result.code, result.errorBody))
                }

                is ApiResult.Exception -> {
                    Result.Failure((result.throwable as? Exception)?.toAppError() ?: AppError.Unknown)
                }
            }
        }

    private inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
        when (this) {
            is Result.Success -> Result.Success(transform(value))
            is Result.Failure -> this
        }
}
