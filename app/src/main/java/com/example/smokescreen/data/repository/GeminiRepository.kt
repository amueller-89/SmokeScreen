package com.example.smokescreen.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.smokescreen.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GeminiRepository(private val context: Context) {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    
    private fun readPromptFromAssets(): String {
        return try {
            context.assets.open("gemini_prompt.txt").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("GeminiRepository", "Error reading prompt file", e)
            "Analyze these bar/pub images and describe the atmosphere, style, and vibe of this establishment."
        }
    }
    
    suspend fun analyzeImages(
        images: List<ByteArray>,
        customPrompt: String? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (images.isEmpty()) {
                    return@withContext Result.failure(Exception("No images provided"))
                }
                
                // Get prompt from file or use custom prompt
                val prompt = customPrompt ?: readPromptFromAssets()
                
                // Create content with text and images
                val inputContent = content {
                    text(prompt)
                    for (imageBytes in images) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        if (bitmap != null) {
                            image(bitmap)
                        }
                    }
                }
                
                val response = generativeModel.generateContent(inputContent)
                val result = response.text ?: "No response generated"
                
                // Print to console as requested
                Log.d("GeminiAnalysis", "Prompt: $prompt")
                Log.d("GeminiAnalysis", "Response: $result")
                println("=== GEMINI ANALYSIS ===")
                println("Prompt: $prompt")
                println("Response: $result")
                println("=====================")
                
                Result.success(result)
            } catch (e: Exception) {
                Log.e("GeminiRepository", "Error analyzing images", e)
                Result.failure(e)
            }
        }
    }
}