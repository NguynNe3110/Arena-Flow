package com.uzuu.team.domain.model

data class TeamDraft(
    val name: String,
    val shortName: String = "",
    val description: String = "",
    val ward: String = "",
    val skillLevels: List<String> = emptyList(),
)
