package com.otaku.kickassanime.page

import com.google.gson.Gson
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import com.otaku.kickassanime.di.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
@Module
class TestFakeDependencies {
    /**
     * We can use retrofit mock servers to mock network calls useful in test
     */
    private val mockWebServer = MockWebServer()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(0, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .client(client)

    @Provides
    @Singleton
    @Named("kickass")
    fun retrofit(gson: Gson): Retrofit = retrofit
        .addConverterFactory(FindJsonInTextConverterFactory.create(gson))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun mockServer() = mockWebServer
}