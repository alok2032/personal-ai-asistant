package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SearchHistoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SearchHistoryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(record: SearchHistoryRecord): Long

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun deleteRecord(record: SearchHistoryRecord)

    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM search_history WHERE query LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchRecords(searchQuery: String): Flow<List<SearchHistoryRecord>>
}
