package com.igorwojda.showcase.app

import android.app.Application
import com.igorwojda.showcase.app.di.appModule
import com.igorwojda.showcase.feature.admin.featureAdminModules
import com.igorwojda.showcase.feature.auth.featureAuthModules
import com.igorwojda.showcase.feature.base.featureBaseModules
import com.igorwojda.showcase.feature.chat.featureChatModules
import com.igorwojda.showcase.feature.match.featureMatchModules
import com.igorwojda.showcase.feature.mercenary.featureMercenaryModules
import com.igorwojda.showcase.feature.notification.featureNotificationModules
import com.igorwojda.showcase.feature.pairing.featurePairingModules
import com.igorwojda.showcase.feature.profile.featureProfileModules
import com.igorwojda.showcase.feature.reputation.featureReputationModules
import com.igorwojda.showcase.feature.team.featureTeamModules
import com.igorwojda.showcase.feature.venue.featureVenueModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import timber.log.Timber

class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin()
        initTimber()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@ShowcaseApplication)

            modules(appModule)
            modules(featureBaseModules)
            modules(featureAuthModules)
            modules(featureProfileModules)
            modules(featureTeamModules)
            modules(featureVenueModules)
            modules(featureMatchModules)
            modules(featurePairingModules)
            modules(featureMercenaryModules)
            modules(featureChatModules)
            modules(featureNotificationModules)
            modules(featureReputationModules)
            modules(featureAdminModules)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
