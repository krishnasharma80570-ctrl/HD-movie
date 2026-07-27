package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieCatalog
import com.example.data.MovieItem
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.MovieCard
import com.example.ui.theme.CinemaRed
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun BrowseScreen(
    viewModel: MovieViewModel,
    onMovieClick: (MovieItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val movies by viewModel.filteredMovies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val watchlistIds = watchlist.map { it.id }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0E))
            .padding(top = 8.dp)
    ) {
        // Search TextField Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Search movies, genres...", color = Color.Gray, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CinemaRed
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.LightGray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF141620),
                    unfocusedContainerColor = Color(0xFF141620),
                    focusedBorderColor = CinemaRed,
                    unfocusedBorderColor = Color(0xFF232634),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Category Filter Chips
        CategoryChipRow(
            categories = MovieCatalog.categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.setCategory(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Movies Count / Filter info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${movies.size} Movies Found",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (searchQuery.isNotBlank() || selectedCategory != "All") {
                Text(
                    text = "Filtered by $selectedCategory ${if (searchQuery.isNotBlank()) "\"$searchQuery\"" else ""}",
                    color = CinemaRed,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No movies match your search",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try searching for a different title or genre",
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onWatchlistToggle = { viewModel.toggleWatchlist(movie) },
                        isInWatchlist = watchlistIds.contains(movie.id)
                    )
                }
            }
        }
    }
}
