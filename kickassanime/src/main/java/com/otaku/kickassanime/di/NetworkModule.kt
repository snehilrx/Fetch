package com.otaku.kickassanime.di

import android.util.Log
import com.google.gson.Gson
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    @Named("kickass")
    fun kickass(gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(FindJsonInTextConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .baseUrl(Strings.KICKASSANIME_URL)
        .build()

    @Provides
    @Singleton
    @Named("animeskip")
    fun animeskip(logger: HttpLoggingInterceptor): Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(logger)
            .addInterceptor {
                return@addInterceptor it.proceed(
                    it.request().newBuilder()
                        .addHeader("X-Client-ID", "3LqFWBqcnwlyruQE8QSvEX6X2KjDrIun").build()
                )
            }.addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.v("Ok2Curl", message)
                }
            })).build())
        .baseUrl(Strings.ANIME_SKIP_URL)
        .build()
}