@file:Suppress("ComplexInterface")

package com.igorwojda.showcase.feature.mercenary.domain.repository

import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.CreateMercenaryPostParams
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import com.igorwojda.showcase.feature.mercenary.domain.model.UpdateMercenaryPostParams

/** Repository contract for mercenary recruitment posts and applications. */
@Suppress("TooManyFunctions")
interface MercenaryRepository {
    suspend fun getMercenaryPosts(filter: MercenaryPostFilter): Result<MercenaryPage>

    suspend fun getMyTeamPosts(
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<MercenaryPage>

    suspend fun getMercenaryPostDetail(postId: String): Result<MercenaryPostDetail>

    suspend fun createMercenaryPost(params: CreateMercenaryPostParams): Result<MercenaryPostDetail>

    suspend fun updateMercenaryPost(params: UpdateMercenaryPostParams): Result<MercenaryPostDetail>

    suspend fun closeMercenaryPost(postId: String): Result<Unit>

    suspend fun applyToMercenaryPost(
        postId: String,
        position: PlayerPosition,
        note: String,
    ): Result<MercenaryApplication>

    suspend fun getPostApplications(postId: String): Result<List<MercenaryApplication>>

    suspend fun acceptApplication(
        postId: String,
        appId: String,
    ): Result<MercenaryPostDetail>

    suspend fun rejectApplication(
        postId: String,
        appId: String,
    ): Result<Unit>

    suspend fun withdrawApplication(
        postId: String,
        appId: String,
    ): Result<Unit>

    suspend fun getMyApplications(): Result<List<MercenaryApplication>>
}
