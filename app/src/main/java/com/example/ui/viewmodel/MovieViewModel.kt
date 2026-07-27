package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MovieCatalog
import com.example.data.MovieDatabase
import com.example.data.MovieItem
import com.example.data.WatchHistoryEntity
import com.example.data.WatchlistEntity
import com.example.download.DownloadItem
import com.example.download.Mp4DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MovieDatabase.getDatabase(application)
    private val dao = db.movieDao()
    private val downloadManager = Mp4DownloadManager(application)

    val downloads: StateFlow<List<DownloadItem>> = downloadManager.downloads

    private val _detectedVideos = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val detectedVideos: StateFlow<List<Pair<String, String>>> = _detectedVideos.asStateFlow()

    private val _downloadToastMessage = MutableStateFlow<String?>(null)
    val downloadToastMessage: StateFlow<String?> = _downloadToastMessage.asStateFlow()

    private val _movies = MutableStateFlow<List<MovieItem>>(MovieCatalog.sampleMovies)
    val movies: StateFlow<List<MovieItem>> = _movies.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMovie = MutableStateFlow<MovieItem?>(null)
    val selectedMovie: StateFlow<MovieItem?> = _selectedMovie.asStateFlow()

    private val _activeStreamMovie = MutableStateFlow<MovieItem?>(null)
    val activeStreamMovie: StateFlow<MovieItem?> = _activeStreamMovie.asStateFlow()

    private val _activeStreamUrl = MutableStateFlow<String?>(null)
    val activeStreamUrl: StateFlow<String?> = _activeStreamUrl.asStateFlow()

    private val _totalAdsBlocked = MutableStateFlow(42)
    val totalAdsBlocked: StateFlow<Int> = _totalAdsBlocked.asStateFlow()

    fun startMp4Download(url: String, fileName: String, userAgent: String? = null) {
        val id = downloadManager.startDownload(url, fileName, userAgent)
        _downloadToastMessage.value = "Started downloading: $fileName"
    }

    fun downloadMovie(movie: MovieItem) {
        val downloadUrl = if (movie.streamUrl.endsWith(".mp4", ignoreCase = true)) {
            movie.streamUrl
        } else {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }
        val fileName = "${movie.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_HD.mp4"
        startMp4Download(downloadUrl, fileName)
    }

    fun cancelDownload(id: String) {
        downloadManager.cancelDownload(id)
    }

    fun deleteDownload(id: String) {
        downloadManager.deleteDownload(id)
    }

    fun addDetectedVideo(url: String, title: String) {
        val current = _detectedVideos.value.toMutableList()
        if (current.none { it.first == url }) {
            current.add(0, url to title)
            _detectedVideos.value = current.take(10) // keep top 10 detected
        }
    }

    fun clearDetectedVideos() {
        _detectedVideos.value = emptyList()
    }

    fun clearDownloadToast() {
        _downloadToastMessage.value = null
    }

    val watchlist = dao.getWatchlist().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val watchHistory = dao.getWatchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredMovies: StateFlow<List<MovieItem>> = combine(
        _movies, _selectedCategory, _searchQuery
    ) { moviesList, category, query ->
        moviesList.filter { movie ->
            val matchesCategory = (category == "All" || movie.genre.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() || movie.title.contains(query, ignoreCase = true) ||
                    movie.genre.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MovieCatalog.sampleMovies
    )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMovie(movie: MovieItem?) {
        _selectedMovie.value = movie
    }

    fun playStream(movie: MovieItem, url: String? = null) {
        _activeStreamMovie.value = movie
        _activeStreamUrl.value = url ?: movie.streamUrl
        
        // Record watch history
        viewModelScope.launch {
            dao.saveWatchHistory(
                WatchHistoryEntity(
                    id = movie.id,
                    title = movie.title,
                    posterUrl = movie.posterUrl,
                    genre = movie.genre,
                    streamUrl = url ?: movie.streamUrl
                )
            )
        }
    }

    fun playCustomUrl(url: String, title: String = "HD Stream") {
        val dummyMovie = MovieItem(
            id = "custom_" + System.currentTimeMillis(),
            title = title,
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "",
            rating = "HD",
            year = "2026",
            duration = "",
            genre = "Stream",
            overview = "Live Stream from Cinema Engine",
            streamUrl = url
        )
        _activeStreamMovie.value = dummyMovie
        _activeStreamUrl.value = url
    }

    fun closeStream() {
        _activeStreamMovie.value = null
        _activeStreamUrl.value = null
    }

    fun recordAdsBlocked(count: Int) {
        _totalAdsBlocked.value += count
    }

    fun toggleWatchlist(movie: MovieItem) {
        viewModelScope.launch {
            val currentList = watchlist.value
            val isAlreadyIn = currentList.any { it.id == movie.id }
            if (isAlreadyIn) {
                dao.removeFromWatchlist(movie.id)
            } else {
                dao.addToWatchlist(
                    WatchlistEntity(
                        id = movie.id,
                        title = movie.title,
                        posterUrl = movie.posterUrl,
                        rating = movie.rating,
                        year = movie.year,
                        genre = movie.genre,
                        streamUrl = movie.streamUrl
                    )
                )
            }
        }
    }
}
