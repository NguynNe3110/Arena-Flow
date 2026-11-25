package com.igorwojda.showcase.feature.team.domain

import com.igorwojda.showcase.feature.team.domain.usecase.AcceptInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.CreateTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.DisbandTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetCaptainHistoryUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyInvitationsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetSystemTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamMembersUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.InviteTeamMemberUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.LeaveTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.RejectInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.SearchInvitationCandidatesUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.TransferCaptainUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.UpdateTeamUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetMyTeamsUseCase)
        singleOf(::GetSystemTeamsUseCase)
        singleOf(::GetTeamDetailUseCase)
        singleOf(::CreateTeamUseCase)
        singleOf(::UpdateTeamUseCase)
        singleOf(::DisbandTeamUseCase)
        singleOf(::TransferCaptainUseCase)
        singleOf(::GetCaptainHistoryUseCase)
        singleOf(::GetMyInvitationsUseCase)
        singleOf(::AcceptInvitationUseCase)
        singleOf(::RejectInvitationUseCase)
        singleOf(::GetTeamMembersUseCase)
        singleOf(::SearchInvitationCandidatesUseCase)
        singleOf(::InviteTeamMemberUseCase)
        singleOf(::LeaveTeamUseCase)
    }
