package com.igorwojda.showcase.feature.team.presentation

import com.igorwojda.showcase.feature.team.presentation.screen.teamdetail.TeamDetailViewModel
import com.igorwojda.showcase.feature.team.presentation.screen.teamform.TeamFormViewModel
import com.igorwojda.showcase.feature.team.presentation.screen.teamhistory.TeamHistoryViewModel
import com.igorwojda.showcase.feature.team.presentation.screen.teamhub.TeamHubViewModel
import com.igorwojda.showcase.feature.team.presentation.screen.teaminvitation.TeamInvitationViewModel
import com.igorwojda.showcase.feature.team.presentation.screen.teammanage.TeamManageViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::TeamHubViewModel)
        viewModelOf(::TeamFormViewModel)
        viewModelOf(::TeamDetailViewModel)
        viewModelOf(::TeamManageViewModel)
        viewModelOf(::TeamHistoryViewModel)
        viewModelOf(::TeamInvitationViewModel)
    }
