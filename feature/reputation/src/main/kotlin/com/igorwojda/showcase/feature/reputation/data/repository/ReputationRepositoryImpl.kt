package com.igorwojda.showcase.feature.reputation.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitChatReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchRatingRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationCommandRetrofitService
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationQueryRetrofitService
import com.igorwojda.showcase.feature.reputation.data.mapper.toDomain
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeSubmission
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.SubmitRatingParams
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Repository implementation backed by REPUTATION-001 Retrofit services. */
internal class ReputationRepositoryImpl(
    private val reputationQueryRetrofitService: ReputationQueryRetrofitService,
    private val reputationCommandRetrofitService: ReputationCommandRetrofitService,
) : ReputationRepository {
    override suspend fun getMatchRatings(
        matchId: String,
        teamId: String,
    ): Result<MatchRatings> =
        executeApi {
            reputationQueryRetrofitService.getMatchRatings(matchId = matchId, teamId = teamId)
        }.mapSuccess { it.data.toDomain() }

    override suspend fun submitMatchRating(params: SubmitRatingParams): Result<MatchRating> =
        executeApi {
            reputationCommandRetrofitService.submitMatchRating(
                matchId = params.matchId,
                request =
                    SubmitMatchRatingRequestDto(
                        isNoShow = params.isNoShow,
                        score = params.score,
                        comment = params.comment?.trim()?.takeIf { it.isNotEmpty() },
                        raterTeamScore = params.raterTeamScore,
                        rateeTeamScore = params.rateeTeamScore,
                    ),
            )
        }.mapSuccess { it.data.toDomain() }

    override suspend fun submitMatchReport(
        matchId: String,
        reportType: ReportType,
        reason: String,
    ): Result<TeamReport> =
        executeApi {
            reputationCommandRetrofitService.submitMatchReport(
                matchId = matchId,
                request =
                    SubmitMatchReportRequestDto(
                        reportType = reportType.apiValue,
                        reason = reason.trim(),
                    ),
            )
        }.mapSuccess { it.data.toDomain() }

    override suspend fun submitChatReport(
        conversationId: String,
        reason: String,
    ): Result<TeamReport> =
        executeApi {
            reputationCommandRetrofitService.submitChatReport(
                conversationId = conversationId,
                request = SubmitChatReportRequestDto(reason = reason.trim()),
            )
        }.mapSuccess { it.data.toDomain() }

    override suspend fun getTeamReports(
        teamId: String,
        direction: ReportDirection,
        page: Int,
        pageSize: Int,
    ): Result<TeamReportPage> =
        executeApi {
            reputationQueryRetrofitService.getTeamReports(
                teamId = teamId,
                direction = direction.apiValue,
                page = page,
                pageSize = pageSize,
            )
        }.mapSuccess { it.toDomain() }

    override suspend fun disputeReport(
        reportId: String,
        evidence: String,
    ): Result<DisputeSubmission> =
        executeApi {
            reputationCommandRetrofitService.disputeReport(
                reportId = reportId,
                request = DisputeReportRequestDto(evidence = evidence.trim()),
            )
        }.mapSuccess { it.data.toDomain() }

    override suspend fun getTeamReputation(teamId: String): Result<TeamReputation> =
        executeApi {
            reputationQueryRetrofitService.getTeamReputation(teamId = teamId)
        }.mapSuccess { it.data.toDomain() }

    private suspend fun <T> executeApi(block: suspend () -> ApiResult<T>): Result<T> =
        dataResult {
            when (val result = block()) {
                is ApiResult.Success -> Result.Success(result.data)
                is ApiResult.Error -> Result.Failure(apiErrorToAppError(result.code, result.errorBody))
                is ApiResult.Exception -> Result.Failure((result.throwable as? Exception)?.toAppError() ?: AppError.Unknown)
            }
        }

    private inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> =
        when (this) {
            is Result.Success -> Result.Success(transform(value))
            is Result.Failure -> this
        }
}
