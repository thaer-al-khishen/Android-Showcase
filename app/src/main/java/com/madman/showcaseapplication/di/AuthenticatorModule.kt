package com.madman.showcaseapplication.di

import com.madman.showcaseapplication.data.networking.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticatorModule {

    @Provides
    @Singleton
    @Named("OkHttpClient")
    fun provideOkHttpClient() = run {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    @Named("Retrofit")
    fun provideRetrofitRefreshToken(
        @Named("OkHttpClient") okHttpClient: OkHttpClient,
        BASE_URL: String
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("RefreshTokenApiService")
    fun provideApiServiceRefreshToken(
        @Named("RetrofitRefreshToken")
        retrofit: Retrofit
    ): ApiService =
        retrofit.create(ApiService::class.java)

}
