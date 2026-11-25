package com.igorwojda.showcase.feature.venue.presentation

import com.igorwojda.showcase.feature.venue.presentation.screen.venuedetail.VenueDetailViewModel
import com.igorwojda.showcase.feature.venue.presentation.screen.venueform.VenueFormViewModel
import com.igorwojda.showcase.feature.venue.presentation.screen.venuephotos.VenuePhotosViewModel
import com.igorwojda.showcase.feature.venue.presentation.screen.venuepicker.VenuePickerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::VenuePickerViewModel)
        viewModelOf(::VenueDetailViewModel)
        viewModelOf(::VenueFormViewModel)
        viewModelOf(::VenuePhotosViewModel)
    }
