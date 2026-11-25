package com.igorwojda.showcase.feature.match.data

import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchCommandRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchQueryRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MatchRequestRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchCommandRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchQueryRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchRequestRetrofitService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val variantMatchDataModule =
    module {
        singleOf(::MockMatchQueryRetrofitService) { bind<MatchQueryRetrofitService>() }
        singleOf(::MockMatchCommandRetrofitService) { bind<MatchCommandRetrofitService>() }
        singleOf(::MockMatchRequestRetrofitService) { bind<MatchRequestRetrofitService>() }
    }
