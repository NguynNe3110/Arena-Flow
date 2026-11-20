package com.uzuu.mercenary.data.datasource.api

import com.uzuu.mercenary.domain.model.MercenaryPost

interface MercenaryRemoteDataSource {
    suspend fun getMercenaryPosts(): List<MercenaryPost>

    suspend fun applyMercenary(
        postId: String,
        message: String,
    ): MercenaryPost
}
