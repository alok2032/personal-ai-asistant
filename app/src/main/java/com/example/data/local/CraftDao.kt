package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CreativeCraft
import com.example.data.model.Saved3DModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CraftDao {
    @Query("SELECT * FROM creative_crafts ORDER BY timestamp DESC")
    fun getAllCrafts(): Flow<List<CreativeCraft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCraft(craft: CreativeCraft): Long

    @Delete
    suspend fun deleteCraft(craft: CreativeCraft)

    @Query("SELECT * FROM three_d_models ORDER BY timestamp DESC")
    fun getAll3DModels(): Flow<List<Saved3DModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert3DModel(model: Saved3DModel): Long

    @Delete
    suspend fun delete3DModel(model: Saved3DModel)
}
