package com.uzuu.mercenary.data.repository

import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.data.datasource.api.MercenaryRemoteDataSource
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.repository.MercenaryRepository

internal class MercenaryRepositoryImpl(
    private val mercenaryRemoteDataSource: MercenaryRemoteDataSource,
) : MercenaryRepository {
    override suspend fun getMercenaryPosts(): Result<List<MercenaryPost>> =
        dataResult {
            Result.Success(mercenaryRemoteDataSource.getMercenaryPosts())
        }

    override suspend fun applyMercenary(
        postId: String,
        message: String,
    ): Result<MercenaryPost> =
        dataResult {
            Result.Success(mercenaryRemoteDataSource.applyMercenary(postId, message))
        }
}
