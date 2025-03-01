package com.example.getwhatsnew.data.repository

import com.example.getwhatsnew.data.model.NewsItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface NewsApiService {
    @GET("your_api_endpoint")
    suspend fun getNews(): List<NewsItem>
}

class NewsRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("your_api_gateway_endpoint")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(NewsApiService::class.java)

    suspend fun fetchNews(): List<NewsItem> {
        return apiService.getNews()
    }
}
