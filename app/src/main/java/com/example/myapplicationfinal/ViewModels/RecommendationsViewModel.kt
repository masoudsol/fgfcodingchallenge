package com.example.myapplicationfinal.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfinal.Models.ApiResponse
import com.example.myapplicationfinal.Models.Recommendation
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class RecommendationsViewModel : androidx.lifecycle.ViewModel() {
    var recommendations by mutableStateOf<List<Recommendation>>(emptyList())
        private set
    var likes = mutableStateMapOf<String, Boolean>()
        private set
    var comments = mutableStateMapOf<String, MutableList<String>>()
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var isLoadingMore by mutableStateOf(false)
        private set

    init {
        fetchRecommendations()
    }

    fun fetchRecommendations() {
        isRefreshing = true
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                URL("https://d2c9087llvttmg.cloudfront.net/trending_and_sophi/recommendations.json").readText()
            }
            recommendations = Gson().fromJson(json, ApiResponse::class.java).recommendations
            isRefreshing = false
        }
    }

    fun loadMoreRecommendations() {
        if (isLoadingMore) return
        isLoadingMore = true
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                URL("https://d2c9087llvttmg.cloudfront.net/trending_and_sophi/recommendations.json").readText()
            }
            recommendations = recommendations + Gson().fromJson(json, ApiResponse::class.java).recommendations
            isLoadingMore = false
        }
    }

    fun toggleLike(title: String) {
        likes[title] = !(likes[title] ?: false)
    }

    fun addComment(title: String, comment: String) {
        comments.getOrPut(title) { mutableListOf() }.add(comment)
    }
}