package com.uzuu.match.data.datasource.api

import android.content.Context
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.uzuu.match.data.datasource.api.response.MatchListResponse
import com.uzuu.match.data.datasource.api.service.MatchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class MockMatchDataSource(
    private val context: Context,
    private val json: Json,
) : MatchApi {

    override suspend fun getMatches(): ApiResult<MatchListResponse> {
        return withContext(Dispatchers.IO) {
            val content = context.assets
                .open("mock/matches.json")
                .bufferedReader()
                .use { reader -> reader.readText() }

            val response =
                json.decodeFromString<MatchListResponse>(content)

            ApiResult.Success(response)
        }
    }
}
