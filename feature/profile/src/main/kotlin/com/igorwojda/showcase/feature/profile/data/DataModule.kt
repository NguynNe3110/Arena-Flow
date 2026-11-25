package com.igorwojda.showcase.feature.profile.data

import com.igorwojda.showcase.feature.profile.data.repository.ProfileRepositoryImpl
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule =
    module {
        includes(variantProfileDataModule)
        singleOf(::ProfileRepositoryImpl) { bind<ProfileRepository>() }
    }
