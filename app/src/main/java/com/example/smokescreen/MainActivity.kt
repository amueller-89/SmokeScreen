package com.example.smokescreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.smokescreen.data.models.Place
import com.example.smokescreen.ui.theme.SmokeScreenTheme
import com.example.smokescreen.ui.viewmodel.PlacesViewModel
import android.graphics.BitmapFactory

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PlacesViewModel = viewModel { PlacesViewModel(context) }
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
            placeholder = { Text("Henrietta") },
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
        
        // Selected place and photo loading status
        viewModel.selectedPlace?.let { selectedPlace ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected: ${selectedPlace.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (viewModel.isLoadingPhotos) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text("Loading photos...")
                        }
                    } else if (viewModel.placePhotos.isNotEmpty()) {
                        Text(
                            text = "Photos (${viewModel.placePhotos.size}):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.placePhotos) { photoBytes ->
                                PhotoThumbnail(photoBytes = photoBytes)
                            }
                        }
                    } else {
                        Text("No photos available")
                    }
                    
                    // Gemini Analysis Button
                    if (viewModel.placePhotos.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.analyzeWithGemini() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isAnalyzingWithGemini
                        ) {
                            if (viewModel.isAnalyzingWithGemini) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (viewModel.isAnalyzingWithGemini) "Analyzing with Gemini..." else "Analyze with Gemini AI")
                        }
                    }
                    
                    // Gemini Analysis Result
                    viewModel.geminiAnalysis?.let { analysis ->
                        SmokingScoreCard(analysis = analysis)
                    }
                    
                    Button(
                        onClick = { viewModel.deselectPlace() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Deselect")
                    }
                }
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
                    PlaceCard(
                        place = place,
                        isSelected = place.placeId == viewModel.selectedPlace?.placeId,
                        onPlaceClick = { viewModel.selectPlace(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: Place,
    isSelected: Boolean = false,
    onPlaceClick: (Place) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaceClick(place) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                           else MaterialTheme.colorScheme.surface
        )
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

@Composable
fun PhotoThumbnail(
    photoBytes: ByteArray,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(photoBytes) {
        try {
            BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    bitmap?.let { imageBitmap ->
        Card(
            modifier = modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.Image(
                bitmap = imageBitmap,
                contentDescription = "Place photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } ?: run {
        // Fallback for failed image decoding
        Card(
            modifier = modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SmokingScoreCard(analysis: String) {
    // Parse the analysis to extract score and keywords
    val (score, keywords) = parseGeminiAnalysis(analysis)
    val scoreColor = getScoreColor(score)
    val scoreLabel = getScoreLabel(score)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Smoking Policy Analysis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Score circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .drawWithContent {
                            drawRect(scoreColor)
                            drawContent()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column {
                    Text(
                        text = scoreLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = scoreColor
                    )
                    Text(
                        text = "Score: $score/3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (keywords.isNotEmpty()) {
                Text(
                    text = "Analysis: $keywords",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun parseGeminiAnalysis(analysis: String): Pair<Int, String> {
    return try {
        // Look for pattern like "2 // some keywords here"
        val parts = analysis.split("//", limit = 2)
        if (parts.size == 2) {
            val score = parts[0].trim().toIntOrNull() ?: 0
            val keywords = parts[1].trim()
            Pair(score.coerceIn(0, 3), keywords)
        } else {
            // Try to extract just a number from the beginning
            val numberMatch = Regex("""^(\d+)""").find(analysis.trim())
            val score = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            Pair(score.coerceIn(0, 3), analysis)
        }
    } catch (e: Exception) {
        Pair(0, analysis)
    }
}

fun getScoreColor(score: Int): Color {
    return when (score) {
        0 -> Color(0xFF4CAF50) // Green - No smoking
        1 -> Color(0xFF8BC34A) // Light green - Probably no smoking
        2 -> Color(0xFFFF9800) // Orange - Possibly smoking
        3 -> Color(0xFFF44336) // Red - Smoking allowed
        else -> Color.Gray
    }
}

fun getScoreLabel(score: Int): String {
    return when (score) {
        0 -> "No Smoking"
        1 -> "Probably No Smoking"
        2 -> "Possibly Smoking"
        3 -> "Smoking Allowed"
        else -> "Unknown"
    }
}

@Preview(showBackground = true)
@Composable
fun PlacesSearchPreview() {
    SmokeScreenTheme {
        PlacesSearchScreen()
    }
}