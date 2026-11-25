package com.igorwojda.showcase.feature.venue.domain

import com.igorwojda.showcase.feature.venue.domain.usecase.CreateVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.DeleteVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueWardsUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenuesUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.ReportVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SetPrimaryVenueImageUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UpdateVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UploadVenueImageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule =
    module {
        singleOf(::GetVenuesUseCase)
        singleOf(::GetVenueDetailUseCase)
        singleOf(::CreateVenueUseCase)
        singleOf(::UpdateVenueUseCase)
        singleOf(::ReportVenueUseCase)
        singleOf(::UploadVenueImageUseCase)
        singleOf(::SetPrimaryVenueImageUseCase)
        singleOf(::DeleteVenueImageUseCase)
        singleOf(::GetVenueWardsUseCase)
        singleOf(::SelectVenueForCallerUseCase)
    }
