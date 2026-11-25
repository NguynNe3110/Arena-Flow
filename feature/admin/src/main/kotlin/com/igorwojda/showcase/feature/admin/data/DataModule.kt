package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.repository.AdminRepositoryImpl
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val adminDataModule =
    module {
        includes(variantAdminDataModule)
        singleOf(::AdminRepositoryImpl) { bind<AdminRepository>() }
    }
