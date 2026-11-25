package com.igorwojda.showcase.feature.mercenary.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.ApplyMercenaryRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.CreateMercenaryPostRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.UpdateMercenaryPostRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.toDomain
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryCommandRetrofitService
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryQueryRetrofitService
import com.igorwojda.showcase.feature.mercenary.domain.model.CreateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import com.igorwojda.showcase.feature.mercenary.domain.model.UpdateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository

/** Network-backed implementation of [MercenaryRepository]. */
@Suppress("TooManyFunctions")
internal class MercenaryRepositoryImpl(
    private val queryService: MercenaryQueryRetrofitService,
    private val commandService: MercenaryCommandRetrofitService,
) : MercenaryRepository {
    override suspend fun getMercenaryPosts(filter: MercenaryPostFilter): Result<MercenaryPage> =
        executeApi {
            queryService.getMercenaryPosts(
                wardId = filter.wardId,
                position = filter.position?.label,
                format = filter.format?.name,
                skillLevel = filter.skillLevel?.label,
                page = filter.page,
                pageSize = filter.pageSize,
            )
        }.map { envelope ->
            MercenaryPage(
                items = envelope.data.map { it.toDomain() },
                page = envelope.pagination.page,
                pageSize = envelope.pagination.pageSize,
                totalPages = envelope.pagination.totalPages,
                totalItems = envelope.pagination.totalItems,
            )
        }

    override suspend fun getMyTeamPosts(
        page: Int,
        pageSize: Int,
    ): Result<MercenaryPage> =
        executeApi { queryService.getMyTeamPosts(page = page, pageSize = pageSize) }
            .map { envelope ->
                MercenaryPage(
                    items = envelope.data.map { it.toDomain() },
                    page = envelope.pagination.page,
                    pageSize = envelope.pagination.pageSize,
                    totalPages = envelope.pagination.totalPages,
                    totalItems = envelope.pagination.totalItems,
                )
            }

    override suspend fun getMercenaryPostDetail(postId: String): Result<MercenaryPostDetail> =
        executeApi { queryService.getMercenaryPostDetail(postId) }
            .map { it.data.toDomain() }

    override suspend fun createMercenaryPost(params: CreateMercenaryPostParams): Result<MercenaryPostDetail> =
        executeApi {
            commandService.createMercenaryPost(
                CreateMercenaryPostRequestDto(
                    teamId = params.teamId,
                    venueId = params.venueId,
                    format = params.format.name,
                    neededPositions = params.neededPositions.map { it.label },
                    slotsNeeded = params.slotsNeeded,
                    allowanceVnd = params.allowanceVnd,
                    matchStartAt = params.matchStartAt,
                    skillLevel = params.skillLevel.label,
                    notes = params.notes,
                ),
            )
        }.map { it.data.toDomain() }

    override suspend fun updateMercenaryPost(params: UpdateMercenaryPostParams): Result<MercenaryPostDetail> =
        executeApi {
            commandService.updateMercenaryPost(
                postId = params.postId,
                request =
                    UpdateMercenaryPostRequestDto(
                        venueId = params.venueId,
                        format = params.format.name,
                        neededPositions = params.neededPositions.map { it.label },
                        slotsNeeded = params.slotsNeeded,
                        allowanceVnd = params.allowanceVnd,
                        matchStartAt = params.matchStartAt,
                        skillLevel = params.skillLevel.label,
                        notes = params.notes,
                    ),
            )
        }.map { it.data.toDomain() }

    override suspend fun closeMercenaryPost(postId: String): Result<Unit> = executeApi { commandService.closeMercenaryPost(postId) }

    override suspend fun applyToMercenaryPost(
        postId: String,
        position: PlayerPosition,
        note: String,
    ): Result<MercenaryApplication> =
        executeApi {
            commandService.applyToMercenaryPost(
                postId = postId,
                request = ApplyMercenaryRequestDto(position = position.label, note = note),
            )
        }.map { it.data.toDomain() }

    override suspend fun getPostApplications(postId: String): Result<List<MercenaryApplication>> =
        executeApi { queryService.getPostApplications(postId) }
            .map { envelope -> envelope.data.map { it.toDomain() } }

    override suspend fun acceptApplication(
        postId: String,
        appId: String,
    ): Result<MercenaryPostDetail> =
        executeApi { commandService.acceptApplication(postId, appId) }
            .map { it.data.toDomain() }

    override suspend fun rejectApplication(
        postId: String,
        appId: String,
    ): Result<Unit> = executeApi { commandService.rejectApplication(postId, appId) }

    override suspend fun withdrawApplication(
        postId: String,
        appId: String,
    ): Result<Unit> = executeApi { commandService.withdrawApplication(postId, appId) }

    override suspend fun getMyApplications(): Result<List<MercenaryApplication>> =
        executeApi { queryService.getMyApplications() }
            .map { envelope -> envelope.data.map { it.toDomain() } }

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
