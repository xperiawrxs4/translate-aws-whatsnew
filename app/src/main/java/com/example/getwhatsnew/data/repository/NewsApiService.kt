package com.example.getwhatsnew.data.repository

import com.example.getwhatsnew.data.model.NewsItem
import retrofit2.http.GET

interface NewsApiService {
    @GET("news")
    suspend fun getNews(): List<NewsItem>
}