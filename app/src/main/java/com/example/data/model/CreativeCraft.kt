package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creative_crafts")
data class CreativeCraft(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "IMAGE", "VIDEO", "PDF"
    val title: String,
    val prompt: String,
    val resultText: String,
    val parametersJson: String? = null,
    val previewAssetUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
