package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val category: String = "CHAT", // "CHAT", "QR", "3D_STUDIO", "IMAGE", "VIDEO", "DOCUMENT"
    val timestamp: Long = System.currentTimeMillis(),
    val previewSummary: String? = null
)
