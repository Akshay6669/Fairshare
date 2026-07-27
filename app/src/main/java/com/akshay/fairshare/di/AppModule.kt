package com.akshay.fairshare.di

import android.content.Context
import androidx.room.Room
import com.akshay.fairshare.BuildConfig
import com.akshay.fairshare.data.local.FairShareDao
import com.akshay.fairshare.data.local.FairShareDatabase
import com.akshay.fairshare.data.remote.FairShareApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): FairShareDatabase =
        Room.databaseBuilder(context, FairShareDatabase::class.java, FairShareDatabase.NAME)
            .build()

    @Provides
    fun dao(database: FairShareDatabase): FairShareDao = database.dao()

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun api(client: OkHttpClient, json: Json): FairShareApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FairShareApi::class.java)
}
