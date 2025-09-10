package com.example.smokescreen.data.repository

import com.example.smokescreen.BuildConfig
import com.example.smokescreen.data.api.PlacesApiService
import com.example.smokescreen.data.models.Place
import com.example.smokescreen.data.models.PlaceDetails
import com.example.smokescreen.data.models.Photo
import com.example.smokescreen.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

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
    
    suspend fun getPlaceDetails(placeId: String): Result<PlaceDetails> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPlaceDetails(
                    placeId = placeId,
                    apiKey = BuildConfig.PLACES_API_KEY
                )
                
                if (response.isSuccessful) {
                    val detailsResponse = response.body()
                    if (detailsResponse?.status == "OK") {
                        Result.success(detailsResponse.result)
                    } else {
                        Result.failure(Exception("API Error: ${detailsResponse?.status}"))
                    }
                } else {
                    Result.failure(Exception("HTTP Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun loadPlacePhotos(placeId: String, maxPhotos: Int = 5): Result<List<ByteArray>> {
        return withContext(Dispatchers.IO) {
            try {
                // First get place details to get photo references
                val detailsResult = getPlaceDetails(placeId)
                if (detailsResult.isFailure) {
                    return@withContext Result.failure(detailsResult.exceptionOrNull() ?: Exception("Failed to get place details"))
                }
                
                val placeDetails = detailsResult.getOrNull()
                val photos = placeDetails?.photos?.take(maxPhotos) ?: emptyList()
                
                if (photos.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                // Load each photo
                val photoByteArrays = mutableListOf<ByteArray>()
                for (photo in photos) {
                    try {
                        val photoResponse = apiService.getPlacePhoto(
                            photoReference = photo.photoReference,
                            apiKey = BuildConfig.PLACES_API_KEY
                        )
                        
                        if (photoResponse.isSuccessful) {
                            photoResponse.body()?.bytes()?.let { bytes ->
                                // Compress the image to create thumbnail
                                val compressedBytes = ImageUtils.compressImageBytes(
                                    imageBytes = bytes,
                                    maxWidth = 200,
                                    maxHeight = 200,
                                    quality = 85
                                )
                                photoByteArrays.add(compressedBytes)
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with other photos if one fails
                        continue
                    }
                }
                
                Result.success(photoByteArrays)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}