package com.igorwojda.showcase.feature.venue.data

import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenuePhotoRetrofitService
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenueRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantVenueDataModule =
    module {
        singleOf(::MockVenueRetrofitService) { bind<VenueRetrofitService>() }
        singleOf(::MockVenuePhotoRetrofitService) { bind<VenuePhotoRetrofitService>() }
    }
