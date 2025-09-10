package com.example.smokescreen.data.api

import com.example.smokescreen.data.models.PlaceDetailsResponse
import com.example.smokescreen.data.models.PlacesResponse
import okhttp3.ResponseBody
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
    
    @GET("place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "place_id,name,formatted_address,rating,price_level,photos,opening_hours,geometry",
        @Query("key") apiKey: String
    ): Response<PlaceDetailsResponse>
    
    @GET("place/photo")
    suspend fun getPlacePhoto(
        @Query("photo_reference") photoReference: String,
        @Query("maxwidth") maxWidth: Int = 800,
        @Query("key") apiKey: String
    ): Response<ResponseBody>
}