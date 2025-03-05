package com.example.getwhatsnew.data.repository

import com.example.getwhatsnew.data.model.NewsItem
import com.example.getwhatsnew.di.NetworkModule

class NewsRepository {
    suspend fun fetchNews(): List<NewsItem> {
        return NetworkModule.apiService.getNews()
    }
}
