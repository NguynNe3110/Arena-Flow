package com.igorwojda.showcase.feature.base.data

import com.igorwojda.showcase.feature.base.data.repository.ReleaseSessionRepositoryImpl
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantBaseDataModule =
    module {
        singleOf(::ReleaseSessionRepositoryImpl) { bind<SessionRepository>() }
    }
