package com.uzuu.mercenary.data

import com.uzuu.mercenary.data.datasource.api.MercenaryRemoteDataSource
import com.uzuu.mercenary.data.datasource.api.MockMercenaryDataSource
import com.uzuu.mercenary.data.repository.MercenaryRepositoryImpl
import com.uzuu.mercenary.domain.repository.MercenaryRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<MercenaryRemoteDataSource> { MockMercenaryDataSource() }
        single<MercenaryRepository> { MercenaryRepositoryImpl(get()) }
    }
