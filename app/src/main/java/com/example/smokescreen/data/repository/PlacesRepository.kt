package com.example.smokescreen.data.repository

import com.example.smokescreen.BuildConfig
import com.example.smokescreen.data.api.PlacesApiService
import com.example.smokescreen.data.models.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlacesRepository(private val apiService: PlacesApiService) {
    
    suspend fun searchBarsAndPubs(query: String): Result<List<Place>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchPlaces(
                    query = "$query bar pub Berlin",
                    apiKey = BuildConfig.PLACES_API_KEY
                )
                
                if (response.isSuccessful) {
                    val placesResponse = response.body()
                    if (placesResponse?.status == "OK") {
                        Result.success(placesResponse.results)
                    } else {
                        Result.failure(Exception("API Error: ${placesResponse?.status}"))
                    }
                } else {
                    Result.failure(Exception("HTTP Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}