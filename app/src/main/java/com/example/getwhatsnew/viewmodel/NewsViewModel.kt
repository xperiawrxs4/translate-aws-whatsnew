package com.example.getwhatsnew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getwhatsnew.data.model.NewsItem
import com.example.getwhatsnew.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class UiState {
    object Loading : UiState()
    data class Success(val news: List<NewsItem>) : UiState()
    data class Error(val message: String) : UiState()
}

class NewsViewModel : ViewModel() {
    private val repository = NewsRepository()
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val news = repository.fetchNews()
                    .sortedByDescending { newsItem -> 
                        try {
                            dateFormat.parse(newsItem.pubdate)?.time
                        } catch (e: Exception) {
                            // 日付のパースに失敗した場合はログを出力
                            println("Failed to parse date: ${newsItem.pubdate}")
                            println("Error: ${e.message}")
                            0L // パースに失敗した場合は最も古い日付として扱う
                        }
                    }
                _uiState.value = UiState.Success(news)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("ニュースの取得に失敗しました。再試行してください。")
            }
        }
    }
}