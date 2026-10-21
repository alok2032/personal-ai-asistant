package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_records")
data class QrRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawContent: String,
    val qrType: String = "TEXT", // "URL", "WIFI", "TEXT", "CONTACT", "EMAIL"
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGenerated: Boolean = false,
    val aiAnalysis: String? = null
)
