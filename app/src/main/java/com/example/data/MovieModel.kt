package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MovieItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val rating: String,
    val year: String,
    val duration: String,
    val genre: String,
    val quality: String = "4K Ultra HD",
    val overview: String,
    val streamUrl: String,
    val altStreamUrls: List<String> = emptyList(),
    val isTrending: Boolean = false,
    val isFeatured: Boolean = false
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterUrl: String,
    val rating: String,
    val year: String,
    val genre: String,
    val streamUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterUrl: String,
    val genre: String,
    val streamUrl: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressSeconds: Long = 0
)

object MovieCatalog {
    val BASE_WEB_URL = "https://hdmove2.im/"

    val categories = listOf(
        "All", "Action", "Sci-Fi", "Drama", "Comedy", "Thriller", "Horror", "Animation", "Romance"
    )

    val sampleMovies = listOf(
        MovieItem(
            id = "movie_item_1",
            title = "Supergirl (2026) Hindi Dubbed",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = "8.8",
            year = "2026",
            duration = "2h 15m",
            genre = "Action",
            quality = "4K HDR",
            overview = "Supergirl arrives in HD Hindi Dubbed stream. Watch complete movie in ultra HD stream with full ad-block protection.",
            streamUrl = "https://hdmove2.im/movies/supergirl-2026-hindi-dubbed/",
            altStreamUrls = listOf(
                "https://hdmove2.im/",
                "https://hdmove2.im/?s=supergirl"
            ),
            isTrending = true,
            isFeatured = true
        ),
        MovieItem(
            id = "movie_item_2",
            title = "Avatar: The Way of Water",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = "8.8",
            year = "2023",
            duration = "3h 12m",
            genre = "Sci-Fi",
            quality = "4K HDR",
            overview = "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora. Once a familiar threat returns to finish what was previously started, Jake must work with Neytiri.",
            streamUrl = "https://hdmove2.im/?s=avatar",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = true,
            isFeatured = false
        ),
        MovieItem(
            id = "movie_item_3",
            title = "Oppenheimer",
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=1200&q=80",
            rating = "8.9",
            year = "2023",
            duration = "3h 00m",
            genre = "Drama",
            quality = "1080p HD",
            overview = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
            streamUrl = "https://hdmove2.im/?s=oppenheimer",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = true,
            isFeatured = false
        ),
        MovieItem(
            id = "movie_item_4",
            title = "Dune: Part Two",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = "8.7",
            year = "2024",
            duration = "2h 46m",
            genre = "Sci-Fi",
            quality = "4K Ultra HD",
            overview = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
            streamUrl = "https://hdmove2.im/?s=dune",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = true,
            isFeatured = true
        ),
        MovieItem(
            id = "movie_item_5",
            title = "Spider-Man: Across the Spider-Verse",
            posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1200&q=80",
            rating = "8.7",
            year = "2023",
            duration = "2h 20m",
            genre = "Animation",
            quality = "1080p HD",
            overview = "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.",
            streamUrl = "https://hdmove2.im/?s=spider-man",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = true,
            isFeatured = false
        ),
        MovieItem(
            id = "movie_item_6",
            title = "The Dark Knight",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1200&q=80",
            rating = "9.0",
            year = "2008",
            duration = "2h 32m",
            genre = "Action",
            quality = "1080p HD",
            overview = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
            streamUrl = "https://hdmove2.im/?s=dark+knight",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = false,
            isFeatured = false
        ),
        MovieItem(
            id = "movie_item_7",
            title = "Interstellar",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1200&q=80",
            rating = "8.7",
            year = "2014",
            duration = "2h 49m",
            genre = "Sci-Fi",
            quality = "4K Ultra HD",
            overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
            streamUrl = "https://hdmove2.im/?s=interstellar",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = false,
            isFeatured = false
        ),
        MovieItem(
            id = "movie_item_8",
            title = "John Wick: Chapter 4",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = "7.8",
            year = "2023",
            duration = "2h 49m",
            genre = "Action",
            quality = "1080p HD",
            overview = "John Wick uncovers a path to defeating The High Table. But before he can earn his freedom, Wick must face off against a new enemy with powerful alliances across the globe.",
            streamUrl = "https://hdmove2.im/?s=john+wick",
            altStreamUrls = listOf(
                "https://hdmove2.im/"
            ),
            isTrending = true,
            isFeatured = false
        )
    )
}

