package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BidiokAiEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AiModelType
import com.example.data.model.ChatMessage
import com.example.data.model.CreativeCraft
import com.example.data.model.QrRecord
import com.example.data.model.Saved3DModel
import com.example.data.model.SearchHistoryRecord
import com.example.data.model.UserMemoryRecord
import com.example.data.sync.CloudSyncManager
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    CHAT,
    QR_SCANNER,
    THREE_D_STUDIO,
    CREATIVE_HUB,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatMessageDao()
    private val qrDao = db.qrDataDao()
    private val craftDao = db.craftDao()
    private val historyDao = db.searchHistoryDao()
    private val memoryDao = db.userMemoryDao()

    val cloudSyncManager = CloudSyncManager(application, viewModelScope)
    val apiKeyPrefs = com.example.data.local.ApiKeyPreferences(application)

    val chatMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qrHistory: StateFlow<List<QrRecord>> = qrDao.getAllQrRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCrafts: StateFlow<List<CreativeCraft>> = craftDao.getAllCrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saved3DModels: StateFlow<List<Saved3DModel>> = craftDao.getAll3DModels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityHistory: StateFlow<List<SearchHistoryRecord>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userMemories: StateFlow<List<UserMemoryRecord>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.CHAT)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedModel = MutableStateFlow(AiModelType.BIDIOK_QUANTUM)
    val selectedModel: StateFlow<AiModelType> = _selectedModel.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _active3DPrompt = MutableStateFlow("Quantum Singularity Core")
    val active3DPrompt: StateFlow<String> = _active3DPrompt.asStateFlow()

    val voiceManager = VoiceAssistantManager(application) { recognizedSpeech ->
        handleVoiceCommand(recognizedSpeech)
    }

    val geminiApiKey = MutableStateFlow(apiKeyPrefs.geminiApiKey)
    val openAiApiKey = MutableStateFlow(apiKeyPrefs.openAiApiKey)
    val claudeApiKey = MutableStateFlow(apiKeyPrefs.claudeApiKey)
    val deepseekApiKey = MutableStateFlow(apiKeyPrefs.deepseekApiKey)
    val groqApiKey = MutableStateFlow(apiKeyPrefs.groqApiKey)

    private val _liveSearchStatus = MutableStateFlow<String?>(null)
    val liveSearchStatus: StateFlow<String?> = _liveSearchStatus.asStateFlow()

    fun updateGeminiApiKey(key: String) {
        apiKeyPrefs.geminiApiKey = key
        geminiApiKey.value = key
    }

    fun updateOpenAiApiKey(key: String) {
        apiKeyPrefs.openAiApiKey = key
        openAiApiKey.value = key
    }

    fun updateClaudeApiKey(key: String) {
        apiKeyPrefs.claudeApiKey = key
        claudeApiKey.value = key
    }

    fun updateDeepseekApiKey(key: String) {
        apiKeyPrefs.deepseekApiKey = key
        deepseekApiKey.value = key
    }

    fun updateGroqApiKey(key: String) {
        apiKeyPrefs.groqApiKey = key
        groqApiKey.value = key
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectModel(model: AiModelType) {
        _selectedModel.value = model
    }

    fun navigateTo3DWithPrompt(prompt: String) {
        _active3DPrompt.value = prompt
        _currentScreen.value = AppScreen.THREE_D_STUDIO
        recordActivity(prompt, "3D_STUDIO")
    }

    fun sendMessage(
        prompt: String,
        deepReasoning: Boolean,
        webSearch: Boolean,
        attachmentText: String? = null,
        attachmentUri: String? = null
    ) {
        if (prompt.isBlank() && attachmentText.isNullOrBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val displayPrompt = if (prompt.isNotBlank()) prompt else "Analyzed Attached File"
            // Record query into activity history
            recordActivity(displayPrompt, "CHAT")

            // Save User Message
            val userMsg = ChatMessage(
                role = "user",
                content = displayPrompt,
                modelType = _selectedModel.value.name,
                attachedImagePath = attachmentUri
            )
            chatDao.insertMessage(userMsg)

            _isAiProcessing.value = true
            _liveSearchStatus.value = "Starting Quantum Consensus for \"${displayPrompt.take(25)}\"..."

            // Fetch active memories and recent search history to inject into AI reasoning
            val activeMemories = memoryDao.getAllMemoriesList()
            val recentSearchesList = activityHistory.value.take(15)

            val response = BidiokAiEngine.processQuery(
                prompt = displayPrompt,
                modelType = _selectedModel.value,
                deepReasoningEnabled = deepReasoning,
                webSearchEnabled = webSearch,
                attachmentText = attachmentText,
                attachmentUri = attachmentUri,
                userMemories = activeMemories,
                recentSearches = recentSearchesList,
                geminiApiKey = apiKeyPrefs.geminiApiKey,
                openAiApiKey = apiKeyPrefs.openAiApiKey,
                claudeApiKey = apiKeyPrefs.claudeApiKey,
                deepseekApiKey = apiKeyPrefs.deepseekApiKey,
                groqApiKey = apiKeyPrefs.groqApiKey,
                onSearchStep = { step ->
                    _liveSearchStatus.value = step
                }
            )

            // If a new memory was discovered in the prompt, persist it!
            response.extractedMemory?.let { newMem ->
                memoryDao.insertMemory(newMem)
            }

            val assistantMsg = ChatMessage(
                role = "assistant",
                content = response.content,
                modelType = _selectedModel.value.name,
                reasoningText = response.reasoning,
                webSources = response.webSources,
                isCraftResult = response.isCraft,
                craftType = response.craftType,
                attachedImagePath = response.imageUrl
            )
            chatDao.insertMessage(assistantMsg)

            _isAiProcessing.value = false
            _liveSearchStatus.value = null

            // Auto-speak if the user initiated via voice
            if (voiceManager.lastSpeechText.value.isNotBlank() && displayPrompt.contains(voiceManager.lastSpeechText.value)) {
                voiceManager.speak(response.content)
            }
        }
    }

    private fun handleVoiceCommand(spokenText: String) {
        val lower = spokenText.lowercase().trim()
        when {
            lower.contains("scan qr") || lower.contains("qr scan") || lower.contains("open scanner") -> {
                _currentScreen.value = AppScreen.QR_SCANNER
                voiceManager.speak("Opening QR Scanner")
            }
            lower.contains("3d model") || lower.contains("create 3d") || lower.contains("mesh studio") -> {
                _active3DPrompt.value = spokenText.replace("3d model", "").replace("create 3d", "").trim()
                _currentScreen.value = AppScreen.THREE_D_STUDIO
                voiceManager.speak("Synthesizing 3D mesh in Studio")
            }
            lower.contains("creative hub") || lower.contains("craft image") || lower.contains("video storyboard") -> {
                _currentScreen.value = AppScreen.CREATIVE_HUB
                voiceManager.speak("Opening Creative Hub")
            }
            lower.contains("profile") || lower.contains("history") || lower.contains("memories") || lower.contains("google account") -> {
                _currentScreen.value = AppScreen.PROFILE
                voiceManager.speak("Opening Profile, Memories, and Activity History")
            }
            lower.contains("clear chat") || lower.contains("delete chat") -> {
                clearChat()
                voiceManager.speak("Chat cleared")
            }
            else -> {
                _currentScreen.value = AppScreen.CHAT
                sendMessage(spokenText, deepReasoning = true, webSearch = true)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearAllMessages()
        }
    }

    fun saveQrRecord(rawContent: String, type: String, aiAnalysis: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = QrRecord(
                rawContent = rawContent,
                qrType = type,
                title = "QR Scan: ${rawContent.take(24)}",
                aiAnalysis = aiAnalysis
            )
            qrDao.insertQrRecord(record)
            recordActivity("Scanned $type: ${rawContent.take(40)}", "QR")
        }
    }

    fun deleteQrRecord(record: QrRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            qrDao.deleteQrRecord(record)
        }
    }

    fun saveCraft(craft: CreativeCraft) {
        viewModelScope.launch(Dispatchers.IO) {
            craftDao.insertCraft(craft)
            recordActivity(craft.title, craft.type)
        }
    }

    fun save3DModel(model: Saved3DModel) {
        viewModelScope.launch(Dispatchers.IO) {
            craftDao.insert3DModel(model)
            recordActivity("3D Model: ${model.title}", "3D_STUDIO")
        }
    }

    fun recordActivity(query: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.insertHistory(
                SearchHistoryRecord(
                    query = query,
                    category = category
                )
            )
        }
    }

    fun deleteHistoryItem(record: SearchHistoryRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.clearAllHistory()
        }
    }

    fun addUserMemory(topic: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryDao.insertMemory(
                UserMemoryRecord(
                    keyTopic = topic.trim(),
                    content = content.trim()
                )
            )
        }
    }

    fun deleteUserMemory(memory: UserMemoryRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryDao.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            memoryDao.clearAllMemories()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
