package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QrRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDataDao {
    @Query("SELECT * FROM qr_records ORDER BY timestamp DESC")
    fun getAllQrRecords(): Flow<List<QrRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrRecord(record: QrRecord): Long

    @Query("DELETE FROM qr_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun deleteQrRecord(record: QrRecord)

    @Query("DELETE FROM qr_records")
    suspend fun clearAll()
}
