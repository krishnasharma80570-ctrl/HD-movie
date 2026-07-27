package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieCatalog
import com.example.data.MovieItem
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.HeroBanner
import com.example.ui.components.MovieCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (MovieItem) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val movies by viewModel.filteredMovies.collectAsState()
    val allMovies by viewModel.movies.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val featuredMovie = allMovies.firstOrNull { it.isFeatured } ?: allMovies.firstOrNull()
    val watchlistIds = watchlist.map { it.id }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0E))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Hero Banner
        featuredMovie?.let { hero ->
            Box(modifier = Modifier.padding(16.dp)) {
                HeroBanner(
                    movie = hero,
                    onWatchClick = { onMovieClick(hero) },
                    onWatchlistToggle = { viewModel.toggleWatchlist(hero) },
                    isInWatchlist = watchlistIds.contains(hero.id)
                )
            }
        }

        // Category Chips
        CategoryChipRow(
            categories = MovieCatalog.categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.setCategory(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Watching Row (If history exists)
        if (watchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Continue Watching",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchHistory) { history ->
                    Surface(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val match = allMovies.find { it.id == history.id }
                                if (match != null) onMovieClick(match)
                                else viewModel.playCustomUrl(history.streamUrl, history.title)
                            },
                        color = Color(0xFF141620)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Resume",
                                tint = CinemaRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = history.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Tap to resume",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Trending & Popular Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Trending Movies",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.clickable { onSeeAllClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See All",
                    color = CinemaRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Trending Cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val trending = movies.filter { it.isTrending }
            val listToDisplay = if (trending.isNotEmpty()) trending else movies
            items(listToDisplay) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    onWatchlistToggle = { viewModel.toggleWatchlist(movie) },
                    isInWatchlist = watchlistIds.contains(movie.id),
                    modifier = Modifier.width(135.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // All HD Movies Grid Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = CinemaGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Popular HD Catalog ($selectedCategory)",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fixed Grid inside Scrollable View
        val chunkedMovies = movies.chunked(2)
        chunkedMovies.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onWatchlistToggle = { viewModel.toggleWatchlist(movie) },
                        isInWatchlist = watchlistIds.contains(movie.id),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
