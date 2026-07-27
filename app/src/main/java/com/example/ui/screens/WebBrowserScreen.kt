package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.adblock.AdBlockWebView
import com.example.data.MovieCatalog
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun WebBrowserScreen(
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AdBlockWebView(
            url = MovieCatalog.BASE_WEB_URL,
            showAddressBar = false,
            onAdCountUpdated = { count ->
                viewModel.recordAdsBlocked(count)
            },
            onDownloadRequested = { url, fileName, userAgent ->
                viewModel.startMp4Download(url, fileName, userAgent)
            },
            onVideoDetected = { url, title ->
                viewModel.addDetectedVideo(url, title)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
