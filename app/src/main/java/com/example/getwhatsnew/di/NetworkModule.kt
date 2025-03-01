package com.example.getwhatsnew.di

import com.example.getwhatsnew.data.repository.NewsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private val retrofit = Retrofit.Builder()
        .baseUrl("your_api_gateway_endpoint")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: NewsApiService = retrofit.create(NewsApiService::class.java)
}
