package com.uzuu.pairing.data

import com.uzuu.pairing.data.datasource.api.MockPairingDataSource
import com.uzuu.pairing.data.datasource.api.PairingRemoteDataSource
import com.uzuu.pairing.data.repository.PairingRepositoryImpl
import com.uzuu.pairing.domain.repository.PairingRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single<PairingRemoteDataSource> { MockPairingDataSource() }
        single<PairingRepository> { PairingRepositoryImpl(get()) }
    }
