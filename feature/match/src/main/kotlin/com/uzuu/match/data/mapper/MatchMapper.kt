package com.uzuu.match.data.mapper

import com.uzuu.match.data.datasource.api.model.MatchSummaryDto
import com.uzuu.match.data.datasource.api.response.PaginationApiDto
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType

internal fun MatchSummaryDto.dtoToDomain(): MatchSummary {
    return MatchSummary(
        matchId = this.matchId,
        type = MatchType.valueOf(this.type), // Enum không hợp lệ được xử lý ở repository.
        authorTeamName = this.authorTeamName,
        venueName = this.venueName,
        skillLevels = this.skillLevels,
    )
}
