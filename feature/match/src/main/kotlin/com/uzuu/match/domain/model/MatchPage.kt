package com.uzuu.match.domain.model

data class MatchPage(
    val matches: List<MatchSummary>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)
