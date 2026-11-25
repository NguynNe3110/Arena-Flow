package com.igorwojda.showcase.feature.base.data

import com.igorwojda.showcase.feature.base.data.repository.DebugSessionRepositoryImpl
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantBaseDataModule =
    module {
        singleOf(::ArenaInMemoryStore)
        singleOf(::DebugSessionRepositoryImpl) { bind<SessionRepository>() }
    }
