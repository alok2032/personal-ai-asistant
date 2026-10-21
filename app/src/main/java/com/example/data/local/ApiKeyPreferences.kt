package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class ApiKeyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bidiok_ai_keys", Context.MODE_PRIVATE)

    var geminiApiKey: String
        get() = prefs.getString("KEY_GEMINI", "") ?: ""
        set(value) = prefs.edit().putString("KEY_GEMINI", value.trim()).apply()

    var openAiApiKey: String
        get() = prefs.getString("KEY_OPENAI", "") ?: ""
        set(value) = prefs.edit().putString("KEY_OPENAI", value.trim()).apply()

    var claudeApiKey: String
        get() = prefs.getString("KEY_CLAUDE", "") ?: ""
        set(value) = prefs.edit().putString("KEY_CLAUDE", value.trim()).apply()

    var deepseekApiKey: String
        get() = prefs.getString("KEY_DEEPSEEK", "") ?: ""
        set(value) = prefs.edit().putString("KEY_DEEPSEEK", value.trim()).apply()

    var groqApiKey: String
        get() = prefs.getString("KEY_GROQ", "") ?: ""
        set(value) = prefs.edit().putString("KEY_GROQ", value.trim()).apply()

    fun hasAnyLiveKey(): Boolean {
        return geminiApiKey.isNotBlank() ||
               openAiApiKey.isNotBlank() ||
               claudeApiKey.isNotBlank() ||
               deepseekApiKey.isNotBlank() ||
               groqApiKey.isNotBlank()
    }
}
