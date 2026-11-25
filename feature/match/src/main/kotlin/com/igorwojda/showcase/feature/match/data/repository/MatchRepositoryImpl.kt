@file:Suppress("LabeledExpression")

package com.igorwojda.showcase.feature.match.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.ForfeitMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchCostItemRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.RejectMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SendMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchCommandRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchQueryRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchRequestRetrofitService
import com.igorwojda.showcase.feature.match.data.mapper.toCreateDto
import com.igorwojda.showcase.feature.match.data.mapper.toDomain
import com.igorwojda.showcase.feature.match.data.mapper.toUpdateDto
import com.igorwojda.showcase.feature.match.domain.model.AcceptRequestResult
import com.igorwojda.showcase.feature.match.domain.model.CancelMatchResult
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.model.SportOption
import com.igorwojda.showcase.feature.match.domain.model.WithdrawRequestResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

@Suppress("TooManyFunctions")
internal class MatchRepositoryImpl(
    private val queryService: MatchQueryRetrofitService,
    private val commandService: MatchCommandRetrofitService,
    private val requestService: MatchRequestRetrofitService,
) : MatchRepository {
    override suspend fun getMatches(filter: MatchFilter): Result<MatchPage> =
        dataResult {
            mapApi(
                queryService.getMatches(
                    type = filter.type?.name,
                    wardId = filter.wardId,
                    format = filter.format,
                    skillLevel = filter.skillLevel,
                    dateFrom = filter.dateFrom,
                    dateTo = filter.dateTo,
                    page = filter.page,
                    pageSize = filter.pageSize,
                ),
            ) { it.toDomain() }
        }

    override suspend fun getMyMatches(
        teamId: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<MatchPage> =
        dataResult {
            mapApi(
                queryService.getMyMatches(
                    teamId = teamId,
                    status = status,
                    page = page,
                    pageSize = pageSize,
                ),
            ) { it.toDomain() }
        }

    override suspend fun getMatchDetail(matchId: String): Result<MatchDetail> =
        dataResult {
            mapApi(queryService.getMatchDetail(matchId)) { it.data.toDomain() }
        }

    override suspend fun createMatch(draft: MatchDraft): Result<MatchDetail> =
        dataResult {
            mapApi(commandService.createMatch(draft.toCreateDto())) { it.data.toDomain() }
        }

    override suspend fun updateMatch(
        matchId: String,
        draft: MatchDraft,
    ): Result<MatchDetail> =
        dataResult {
            mapApi(commandService.updateMatch(matchId, draft.toUpdateDto())) { it.data.toDomain() }
        }

    override suspend fun updateCostAgreement(
        matchId: String,
        costAgreement: MatchCostAgreement,
    ): Result<MatchCostAgreement> =
        dataResult {
            val body =
                CostAgreementRequestDto(
                    costType = costAgreement.costType,
                    costSplit = costAgreement.costSplit,
                    venueAmount = costAgreement.venueAmount,
                    drinksAmount = costAgreement.drinksAmount,
                    customItems = costAgreement.customItems.map { MatchCostItemRequestDto(it.itemName, it.amount) },
                )
            mapApi(commandService.updateCostAgreement(matchId, body)) { it.data.toDomain() }
        }

    override suspend fun cancelMatch(
        matchId: String,
        teamId: String,
        expectedStatus: String,
        reason: String?,
    ): Result<CancelMatchResult> =
        dataResult {
            val body = CancelMatchRequestDto(teamId = teamId, expectedStatus = expectedStatus, reason = reason)
            mapApi(commandService.cancelMatch(matchId, body)) { it.data.toDomain() }
        }

    override suspend fun forfeitMatch(
        matchId: String,
        teamId: String,
        reason: String?,
    ): Result<Unit> =
        dataResult {
            mapApi(commandService.forfeitMatch(matchId, ForfeitMatchRequestDto(teamId = teamId, reason = reason))) {
                return@mapApi
            }
        }

    override suspend fun getMatchRequests(
        matchId: String,
        status: String?,
    ): Result<List<MatchRequest>> =
        dataResult {
            mapApi(requestService.getMatchRequests(matchId = matchId, status = status)) { envelope ->
                envelope.data.map { it.toDomain() }
            }
        }

    override suspend fun sendMatchRequest(
        matchId: String,
        teamId: String,
        message: String?,
    ): Result<MatchRequest> =
        dataResult {
            mapApi(
                requestService.sendMatchRequest(
                    matchId = matchId,
                    body = SendMatchRequestDto(teamId = teamId, message = message),
                ),
            ) { it.data.toDomain() }
        }

    override suspend fun acceptMatchRequest(requestId: String): Result<AcceptRequestResult> =
        dataResult {
            mapApi(requestService.acceptMatchRequest(requestId)) { it.data.toDomain() }
        }

    override suspend fun rejectMatchRequest(
        requestId: String,
        reason: String?,
    ): Result<Unit> =
        dataResult {
            val body = reason?.takeIf { it.isNotBlank() }?.let { RejectMatchRequestDto(it) }
            mapApi(requestService.rejectMatchRequest(requestId, body)) { /* 204 */ }
        }

    override suspend fun withdrawMatchRequest(
        requestId: String,
        expectedStatus: String,
        reason: String?,
    ): Result<WithdrawRequestResult> =
        dataResult {
            val body = WithdrawMatchRequestDto(expectedStatus = expectedStatus, reason = reason)
            mapApi(requestService.withdrawMatchRequest(requestId, body)) { it.data.toDomain() }
        }

    override suspend fun getSports(): Result<List<SportOption>> =
        dataResult {
            mapApi(queryService.getSports()) { envelope -> envelope.data.map { it.toDomain() } }
        }

    private inline fun <T, R> mapApi(
        apiResult: ApiResult<T>,
        transform: (T) -> R,
    ): Result<R> =
        when (apiResult) {
            is ApiResult.Success -> {
                Result.Success(transform(apiResult.data))
            }

            is ApiResult.Error -> {
                Result.Failure(apiErrorToAppError(apiResult.code, apiResult.errorBody))
            }

            is ApiResult.Exception -> {
                val appError = (apiResult.throwable as? Exception)?.toAppError() ?: AppError.Unknown
                Result.Failure(appError)
            }
        }
}
