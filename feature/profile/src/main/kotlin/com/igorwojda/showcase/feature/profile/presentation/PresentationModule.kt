package com.igorwojda.showcase.feature.profile.presentation

import com.igorwojda.showcase.feature.profile.presentation.screen.accountmenu.AccountMenuViewModel
import com.igorwojda.showcase.feature.profile.presentation.screen.myprofile.MyProfileViewModel
import com.igorwojda.showcase.feature.profile.presentation.screen.profileedit.ProfileEditViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule =
    module {
        viewModelOf(::MyProfileViewModel)
        viewModelOf(::ProfileEditViewModel)
        viewModelOf(::AccountMenuViewModel)
    }
