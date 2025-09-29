package com.garrett.bitez.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val GOOGLE_PLACES_API_URL = "https://places.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create an OkHttpClient
    private val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    // Create Retrofit service for google places API
    val placesApiService: PlacesApiService = Retrofit.Builder()
            .baseUrl(GOOGLE_PLACES_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlacesApiService::class.java)
}