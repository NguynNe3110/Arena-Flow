package com.igorwojda.showcase.feature.admin.presentation

import com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole.AdminConsoleViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val adminPresentationModule =
    module {
        viewModelOf(::AdminConsoleViewModel)
    }
