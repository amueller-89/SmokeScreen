package com.example.smokescreen.data.models

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("results")
    val results: List<Place>,
    @SerializedName("status")
    val status: String,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)

data class Place(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("vicinity")
    val vicinity: String?,
    @SerializedName("rating")
    val rating: Double?,
    @SerializedName("price_level")
    val priceLevel: Int?,
    @SerializedName("types")
    val types: List<String>,
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours?
)

data class Geometry(
    @SerializedName("location")
    val location: Location
)

data class Location(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean?
)