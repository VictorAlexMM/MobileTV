package com.iptvplayer.app.core.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkClient {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    fun createOkHttpClient(offlineInterceptor: OfflineModeInterceptor? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)

        // Adiciona User-Agent padrão de player para evitar bloqueio 403 de servidores IPTV
        builder.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            if (original.header("User-Agent") == null) {
                requestBuilder.header("User-Agent", "IPTVSmartPlayer/1.0 (Linux; Android) ExoPlayerLib/Media3")
            }
            chain.proceed(requestBuilder.build())
        }

        if (offlineInterceptor != null) {
            builder.addInterceptor(offlineInterceptor)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        builder.addInterceptor(logging)

        return builder.build()
    }

    fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
