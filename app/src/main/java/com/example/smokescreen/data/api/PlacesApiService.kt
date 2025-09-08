package com.example.smokescreen.data.api

import com.example.smokescreen.data.models.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("location") location: String = "52.5200,13.4050", // Berlin coordinates
        @Query("radius") radius: Int = 15000, // 15km radius
        @Query("type") type: String = "bar|night_club",
        @Query("key") apiKey: String
    ): Response<PlacesResponse>
}