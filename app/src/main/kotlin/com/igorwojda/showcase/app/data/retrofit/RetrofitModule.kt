package com.igorwojda.showcase.app.data.retrofit

import com.igorwojda.showcase.app.BuildConfig
import com.igorwojda.showcase.app.data.retrofit.interceptor.AuthenticationInterceptor
import com.igorwojda.showcase.app.data.retrofit.interceptor.TokenRefreshAuthenticator
import com.igorwojda.showcase.app.data.retrofit.interceptor.UserAgentInterceptor
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResultAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import timber.log.Timber

/** Provides Retrofit, OkHttp and their interceptors for ArenaFlow REST API (`/api/v1`). */
val retrofitModule =
    module {
        singleOf(::AuthenticationInterceptor)
        singleOf(::TokenRefreshAuthenticator)
        singleOf(::UserAgentInterceptor)

        single {
            HttpLoggingInterceptor { message ->
                Timber.tag("Network").d(message)
            }.apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }
        }

        single {
            OkHttpClient
                .Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(get<HttpLoggingInterceptor>())
                    }
                    addInterceptor(get<AuthenticationInterceptor>())
                    addInterceptor(get<UserAgentInterceptor>())
                    authenticator(get<TokenRefreshAuthenticator>())
                }.build()
        }

        single {
            val contentType = "application/json".toMediaType()
            val json =
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    encodeDefaults = true
                }

            @OptIn(ExperimentalSerializationApi::class)
            Retrofit
                .Builder()
                .baseUrl(BuildConfig.GRADLE_API_BASE_URL)
                .client(get())
                .addConverterFactory(json.asConverterFactory(contentType))
                .addCallAdapterFactory(ApiResultAdapterFactory())
                .build()
        }
    }
