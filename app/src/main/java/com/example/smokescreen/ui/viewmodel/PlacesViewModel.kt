package com.example.smokescreen.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokescreen.data.models.Place
import com.example.smokescreen.data.repository.PlacesRepository
import com.example.smokescreen.di.NetworkModule
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val repository: PlacesRepository = NetworkModule.placesRepository
) : ViewModel() {
    
    var searchQuery by mutableStateOf("")
        private set
    
    var searchResults by mutableStateOf<List<Place>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
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
}