package com.uzuu.mercenary.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost

internal interface MercenaryRepository {
    suspend fun getMercenaryPosts(): Result<List<MercenaryPost>>

    suspend fun applyMercenary(
        postId: String,
        message: String,
    ): Result<MercenaryPost>
}
