package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserMemoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMemoryDao {
    @Query("SELECT * FROM user_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<UserMemoryRecord>>

    @Query("SELECT * FROM user_memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesList(): List<UserMemoryRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: UserMemoryRecord): Long

    @Delete
    suspend fun deleteMemory(memory: UserMemoryRecord)

    @Query("DELETE FROM user_memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM user_memories")
    suspend fun clearAllMemories()

    @Query("SELECT * FROM user_memories WHERE content LIKE '%' || :query || '%' OR keyTopic LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<UserMemoryRecord>
}
