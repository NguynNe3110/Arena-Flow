package com.uzuu.match.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.httpError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.data.datasource.api.MatchRemoteDataSource
import com.uzuu.match.data.mapper.dtoToDomain
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.repository.MatchRepository

internal class MatchRepositoryImpl(
    private val matchRemoteDataSource: MatchRemoteDataSource,
) : MatchRepository {
    override suspend fun getMatch(matchId: String): Result<MatchDetail> =
        dataResult {
            when (val result = matchRemoteDataSource.getMatch(matchId)) {
                is ApiResult.Success -> Result.Success(result.data.data.dtoToDomain())
                is ApiResult.Error -> Result.Failure(httpError(result.code))
                is ApiResult.Exception -> throw result.throwable
            }
        }

    override suspend fun getMatches(): Result<MatchPage> =
        dataResult {
            when (val result = matchRemoteDataSource.getMatches()) {
                is ApiResult.Success -> {
                    val response = result.data
                    val pagination = response.pagination

                    Result.Success(
                        MatchPage(
                            matches = response.data.map { it.dtoToDomain() },
                            page = pagination.page,
                            pageSize = pagination.pageSize,
                            totalPages = pagination.totalPages,
                            totalItems = pagination.totalItems,
                        ),
                    )
                }

                is ApiResult.Error -> {
                    Result.Failure(httpError(result.code))
                }

                is ApiResult.Exception -> {
                    throw result.throwable
                }
            }
        }
}
