package com.example.myapplicationfinal.Models

import com.google.gson.annotations.SerializedName

class ApiResponse (
    @SerializedName("recommendations") val recommendations: List<Recommendation>
)

data class Recommendation(
    val title: String,
    val deck: String,
    @SerializedName("promo_image") val promoImage: PromoImage,
)

data class PromoImage(
    @SerializedName("urls")  val urls: PromoUrls
)

data class PromoUrls(
    val `650`: String
)
