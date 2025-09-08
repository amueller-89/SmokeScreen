package com.example.smokescreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smokescreen.data.models.Place
import com.example.smokescreen.ui.theme.SmokeScreenTheme
import com.example.smokescreen.ui.viewmodel.PlacesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmokeScreenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlacesSearchScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PlacesSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: PlacesViewModel = viewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Find Bars & Pubs in Berlin",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Search field
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Enter bar or pub name") },
            placeholder = { Text("e.g., Zur Letzten Instanz") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    viewModel.searchPlaces()
                }
            ),
            singleLine = true
        )
        
        // Search button
        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.searchPlaces()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.searchQuery.isNotBlank() && !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (viewModel.isLoading) "Searching..." else "Search")
        }
        
        // Error message
        viewModel.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Results
        if (viewModel.searchResults.isNotEmpty()) {
            Text(
                text = "Found ${viewModel.searchResults.size} places:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.searchResults) { place ->
                    PlaceCard(place = place)
                }
            }
        }
    }
}

@Composable
fun PlaceCard(place: Place) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            place.vicinity?.let { vicinity ->
                Text(
                    text = vicinity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                place.rating?.let { rating ->
                    Text(
                        text = "⭐ $rating",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                place.priceLevel?.let { priceLevel ->
                    Text(
                        text = "$".repeat(priceLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                place.openingHours?.openNow?.let { openNow ->
                    Text(
                        text = if (openNow) "Open now" else "Closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (openNow) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlacesSearchPreview() {
    SmokeScreenTheme {
        PlacesSearchScreen()
    }
}