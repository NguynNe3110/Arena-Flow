package com.uzuu.match.data

import com.uzuu.match.data.datasource.api.MatchRemoteDataSource
import com.uzuu.match.data.datasource.api.MockMatchDataSource
import com.uzuu.match.data.datasource.api.service.MatchApi
import com.uzuu.match.data.repository.MatchRepositoryImpl
import com.uzuu.match.domain.repository.MatchRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule =
    module {
        // Mock thực hiện cùng contract với API; RemoteDataSource vẫn là class.
        single<MatchApi> {
            MockMatchDataSource(
                context = androidContext(),
                json = Json { ignoreUnknownKeys = true },
            )
        }

        singleOf(::MatchRemoteDataSource) // tham chiếu đến contructor của class (nó cần gì )
        singleOf(::MatchRepositoryImpl) { bind<MatchRepository>() } // cần bin vì nó là interface
    }
