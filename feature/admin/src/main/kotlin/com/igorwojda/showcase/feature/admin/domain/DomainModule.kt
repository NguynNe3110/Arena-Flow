package com.igorwojda.showcase.feature.admin.domain

import com.igorwojda.showcase.feature.admin.domain.usecase.AdjustUserReputationUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ApproveVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.BanUserUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminReportsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminUsersUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAuditLogsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetPendingVenuesUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.RejectVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ResolveAdminReportUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.UnbanUserUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val adminDomainModule =
    module {
        singleOf(::GetAdminReportsUseCase)
        singleOf(::ResolveAdminReportUseCase)
        singleOf(::GetPendingVenuesUseCase)
        singleOf(::ApproveVenueUseCase)
        singleOf(::RejectVenueUseCase)
        singleOf(::GetAdminUsersUseCase)
        singleOf(::BanUserUseCase)
        singleOf(::UnbanUserUseCase)
        singleOf(::AdjustUserReputationUseCase)
        singleOf(::GetAuditLogsUseCase)
    }
