package com.igorwojda.showcase.feature.venue.data

import com.igorwojda.showcase.feature.venue.data.repository.VenueRepositoryImpl
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule =
    module {
        includes(variantVenueDataModule)
        singleOf(::VenueRepositoryImpl) { bind<VenueRepository>() }
    }
