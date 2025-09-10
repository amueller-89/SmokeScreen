package com.example.smokescreen.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokescreen.data.models.Place
import com.example.smokescreen.data.repository.PlacesRepository
import com.example.smokescreen.data.repository.GeminiRepository
import com.example.smokescreen.di.NetworkModule
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val context: Context,
    private val repository: PlacesRepository = NetworkModule.placesRepository
) : ViewModel() {
    
    private val geminiRepository: GeminiRepository by lazy {
        NetworkModule.getGeminiRepository(context)
    }
    
    var searchQuery by mutableStateOf("")
        private set
    
    var searchResults by mutableStateOf<List<Place>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedPlace by mutableStateOf<Place?>(null)
        private set
    
    var placePhotos by mutableStateOf<List<ByteArray>>(emptyList())
        private set
    
    var isLoadingPhotos by mutableStateOf(false)
        private set
    
    var isAnalyzingWithGemini by mutableStateOf(false)
        private set
    
    var geminiAnalysis by mutableStateOf<String?>(null)
        private set
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
        errorMessage = null
    }
    
    fun searchPlaces() {
        if (searchQuery.isBlank()) return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            repository.searchBarsAndPubs(searchQuery)
                .onSuccess { places ->
                    searchResults = places
                    isLoading = false
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                    searchResults = emptyList()
                }
        }
    }
    
    fun clearResults() {
        searchResults = emptyList()
        errorMessage = null
    }
    
    fun selectPlace(place: Place) {
        selectedPlace = place
        loadPlacePhotos(place.placeId)
    }
    
    fun deselectPlace() {
        selectedPlace = null
        placePhotos = emptyList()
        geminiAnalysis = null
    }
    
    private fun loadPlacePhotos(placeId: String) {
        viewModelScope.launch {
            isLoadingPhotos = true
            
            repository.loadPlacePhotos(placeId, maxPhotos = 5)
                .onSuccess { photos ->
                    placePhotos = photos
                    isLoadingPhotos = false
                }
                .onFailure { exception ->
                    errorMessage = "Failed to load photos: ${exception.message}"
                    isLoadingPhotos = false
                    placePhotos = emptyList()
                }
        }
    }
    
    fun analyzeWithGemini() {
        if (placePhotos.isEmpty()) return
        
        viewModelScope.launch {
            isAnalyzingWithGemini = true
            geminiAnalysis = null
            

            geminiRepository.analyzeImages(placePhotos)
                .onSuccess { analysis ->
                    geminiAnalysis = analysis
                    isAnalyzingWithGemini = false
                }
                .onFailure { exception ->
                    errorMessage = "Failed to analyze images: ${exception.message}"
                    isAnalyzingWithGemini = false
                }
        }
    }
}