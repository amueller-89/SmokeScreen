package com.example.smokescreen.di

import android.content.Context
import com.example.smokescreen.data.api.PlacesApiService
import com.example.smokescreen.data.repository.PlacesRepository
import com.example.smokescreen.data.repository.GeminiRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val placesApiService: PlacesApiService = retrofit.create(PlacesApiService::class.java)
    
    val placesRepository = PlacesRepository(placesApiService)
    
    fun getGeminiRepository(context: Context): GeminiRepository {
        return GeminiRepository(context)
    }
}