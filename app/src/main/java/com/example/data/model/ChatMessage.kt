package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelType: String = AiModelType.BIDIOK_QUANTUM.name,
    val reasoningText: String? = null,
    val isCraftResult: Boolean = false,
    val craftType: String? = null, // "3D", "IMAGE", "VIDEO", "PDF", "QR"
    val attachedImagePath: String? = null,
    val webSources: String? = null
)
