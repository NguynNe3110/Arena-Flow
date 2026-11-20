package com.uzuu.match.data.datasource.api.response

import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import kotlinx.serialization.Serializable

@Serializable
internal data class MatchListResponse(
    val data: List<MatchSummaryDto>,
    val pagination: PaginationApiDto,
)

@Serializable
internal data class PaginationApiDto(
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)
