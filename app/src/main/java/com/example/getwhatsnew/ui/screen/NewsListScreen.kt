package com.example.getwhatsnew.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getwhatsnew.data.model.NewsItem
import com.example.getwhatsnew.ui.detail.NewsDetailActivity
import com.example.getwhatsnew.viewmodel.NewsViewModel
import com.example.getwhatsnew.viewmodel.UiState

@Composable
fun NewsListScreen(viewModel: NewsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is UiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.news) { news ->
                        NewsItemRow(news) { selectedNews ->
                            val intent = Intent(context, NewsDetailActivity::class.java).apply {
                                putExtra("news_title", selectedNews.title_ja)
                                putExtra("news_description", selectedNews.description_ja)
                                putExtra("news_link", selectedNews.link)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { viewModel.fetchNews() }) {
                        Text("再試行")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItemRow(news: NewsItem, onClick: (NewsItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(news) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = news.title_ja,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = news.pubdate,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
