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
    val openingHours: OpeningHours?,
    @SerializedName("photos")
    val photos: List<Photo>?
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

data class Photo(
    @SerializedName("photo_reference")
    val photoReference: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("width")
    val width: Int,
    @SerializedName("html_attributions")
    val htmlAttributions: List<String>
)

data class PlaceDetailsResponse(
    @SerializedName("result")
    val result: PlaceDetails,
    @SerializedName("status")
    val status: String
)

data class PlaceDetails(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("formatted_address")
    val formattedAddress: String?,
    @SerializedName("rating")
    val rating: Double?,
    @SerializedName("price_level")
    val priceLevel: Int?,
    @SerializedName("photos")
    val photos: List<Photo>?,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours?,
    @SerializedName("geometry")
    val geometry: Geometry
)