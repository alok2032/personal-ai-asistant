package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CreativeCraft
import com.example.data.model.QrRecord
import com.example.data.model.Saved3DModel
import com.example.data.model.SearchHistoryRecord
import com.example.data.model.UserMemoryRecord

@Database(
    entities = [
        ChatMessage::class,
        QrRecord::class,
        CreativeCraft::class,
        Saved3DModel::class,
        SearchHistoryRecord::class,
        UserMemoryRecord::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun qrDataDao(): QrDataDao
    abstract fun craftDao(): CraftDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun userMemoryDao(): UserMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bidiok_ai_vault.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
