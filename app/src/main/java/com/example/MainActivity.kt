package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.HeaderBar
import com.example.ui.components.MovieDetailsBottomSheet
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StreamPlayerScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.screens.WebBrowserScreen
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MovieViewModel

import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.DownloadsScreen

enum class AppTab(val title: String) {
    WEB("Live Stream"),
    DOWNLOADS("MP4 Downloads"),
    HOME("Catalog"),
    WATCHLIST("Watchlist")
}

class MainActivity : ComponentActivity() {

    private val movieViewModel: MovieViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                var currentTab by remember { mutableStateOf(AppTab.WEB) }
                val selectedMovie by movieViewModel.selectedMovie.collectAsState()
                val activeStreamMovie by movieViewModel.activeStreamMovie.collectAsState()
                val activeStreamUrl by movieViewModel.activeStreamUrl.collectAsState()
                val totalAdsBlocked by movieViewModel.totalAdsBlocked.collectAsState()
                val watchlist by movieViewModel.watchlist.collectAsState()
                val downloadToast by movieViewModel.downloadToastMessage.collectAsState()

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val watchlistIds = watchlist.map { it.id }.toSet()

                LaunchedEffect(downloadToast) {
                    downloadToast?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        movieViewModel.clearDownloadToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        HeaderBar(
                            totalAdsBlocked = totalAdsBlocked,
                            onSearchClick = { currentTab = AppTab.HOME },
                            onAdBlockClick = { currentTab = AppTab.WEB }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF10121A),
                            contentColor = Color.White,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.WEB,
                                onClick = { currentTab = AppTab.WEB },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.WEB) Icons.Filled.Language else Icons.Outlined.Language,
                                        contentDescription = "Live Stream"
                                    )
                                },
                                label = { Text("Live Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaRed,
                                    selectedTextColor = CinemaRed,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CinemaRed.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_tab_web")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.DOWNLOADS,
                                onClick = { currentTab = AppTab.DOWNLOADS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.DOWNLOADS) Icons.Filled.Download else Icons.Outlined.Download,
                                        contentDescription = "Downloads"
                                    )
                                },
                                label = { Text("Downloads", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaRed,
                                    selectedTextColor = CinemaRed,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CinemaRed.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_tab_downloads")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.HOME,
                                onClick = { currentTab = AppTab.HOME },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Catalog"
                                    )
                                },
                                label = { Text("Catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaRed,
                                    selectedTextColor = CinemaRed,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CinemaRed.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_tab_home")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.WATCHLIST,
                                onClick = { currentTab = AppTab.WATCHLIST },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.WATCHLIST) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Watchlist"
                                    )
                                },
                                label = { Text("Watchlist", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CinemaRed,
                                    selectedTextColor = CinemaRed,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CinemaRed.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_tab_watchlist")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF0A0B0E))
                    ) {
                        when (currentTab) {
                            AppTab.WEB -> WebBrowserScreen(
                                viewModel = movieViewModel
                            )
                            AppTab.DOWNLOADS -> DownloadsScreen(
                                viewModel = movieViewModel
                            )
                            AppTab.HOME -> HomeScreen(
                                viewModel = movieViewModel,
                                onMovieClick = { movieViewModel.selectMovie(it) },
                                onSeeAllClick = { currentTab = AppTab.HOME }
                            )
                            AppTab.WATCHLIST -> WatchlistScreen(
                                viewModel = movieViewModel,
                                onMovieClick = { movieViewModel.selectMovie(it) }
                            )
                        }

                        // Movie Details Bottom Sheet
                        selectedMovie?.let { movie ->
                            MovieDetailsBottomSheet(
                                movie = movie,
                                sheetState = sheetState,
                                onDismiss = { movieViewModel.selectMovie(null) },
                                onPlayStream = { movieItem, streamUrl ->
                                    movieViewModel.selectMovie(null)
                                    movieViewModel.playStream(movieItem, streamUrl)
                                },
                                onToggleWatchlist = { movieViewModel.toggleWatchlist(movie) },
                                isInWatchlist = watchlistIds.contains(movie.id),
                                onDownloadMovie = { movieItem ->
                                    movieViewModel.downloadMovie(movieItem)
                                }
                            )
                        }

                        // Fullscreen Stream Player
                        AnimatedVisibility(
                            visible = activeStreamMovie != null && activeStreamUrl != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            if (activeStreamMovie != null && activeStreamUrl != null) {
                                StreamPlayerScreen(
                                    movie = activeStreamMovie!!,
                                    streamUrl = activeStreamUrl!!,
                                    onClose = { movieViewModel.closeStream() },
                                    onAdCountUpdated = { count ->
                                        movieViewModel.recordAdsBlocked(count)
                                    },
                                    onDownloadRequested = { url, fileName, userAgent ->
                                        movieViewModel.startMp4Download(url, fileName, userAgent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
