package com.igorwojda.showcase.feature.venue.data

import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenuePhotoRetrofitService
import com.igorwojda.showcase.feature.venue.data.datasource.api.service.VenueRetrofitService
import org.koin.dsl.module
import retrofit2.Retrofit

internal val variantVenueDataModule =
    module {
        single { get<Retrofit>().create(VenueRetrofitService::class.java) }
        single { get<Retrofit>().create(VenuePhotoRetrofitService::class.java) }
    }
