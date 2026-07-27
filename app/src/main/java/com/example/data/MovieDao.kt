package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :movieId)")
    fun isInWatchlist(movieId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE id = :movieId")
    suspend fun removeFromWatchlist(movieId: String)

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 20")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()
}
