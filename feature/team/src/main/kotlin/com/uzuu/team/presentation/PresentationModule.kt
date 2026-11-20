package com.uzuu.team.presentation

import com.uzuu.team.presentation.screen.teamcreate.TeamCreateViewModel
import com.uzuu.team.presentation.screen.teamdetail.TeamDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::TeamDetailViewModel)
        viewModelOf(::TeamCreateViewModel)
    }
