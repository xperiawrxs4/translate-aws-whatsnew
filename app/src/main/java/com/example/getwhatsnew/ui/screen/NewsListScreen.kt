package com.example.getwhatsnew.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getwhatsnew.data.model.NewsItem
import com.example.getwhatsnew.ui.detail.NewsDetailActivity
import com.example.getwhatsnew.viewmodel.NewsViewModel
import com.example.getwhatsnew.viewmodel.UiState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsListScreen(viewModel: NewsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            viewModel.fetchNews()
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success || uiState is UiState.Error) {
            refreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        // 削除
        /*
        when (val currentState = uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        */
        when (val currentState = uiState) {
            is UiState.Loading -> {}

            is UiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentState.news) { newsItem ->
                        NewsItemRow(newsItem) {
                            val intent = Intent(context, NewsDetailActivity::class.java).apply {
                                putExtra("news_title", newsItem.title_ja)
                                putExtra("news_description", newsItem.description_ja)
                                putExtra("news_link", newsItem.link)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.fetchNews() }) {
                        Text(text = "再試行")
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState,
        )
    }
}

@Composable
fun NewsItemRow(newsItem: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = newsItem.title_ja,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Published: ${newsItem.pubdate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
