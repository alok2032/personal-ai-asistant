package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreativeHubScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.ThreeDStudioScreen
import com.example.ui.theme.BidiokTheme
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BidiokTheme {
                BidiokApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BidiokApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val qrHistory by viewModel.qrHistory.collectAsState()
    val saved3DModels by viewModel.saved3DModels.collectAsState()
    val activityHistory by viewModel.activityHistory.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()
    val active3DPrompt by viewModel.active3DPrompt.collectAsState()

    // BackHandler: Navigate back to Chat Screen if in any sub-screen
    if (currentScreen != AppScreen.CHAT) {
        BackHandler {
            viewModel.navigateTo(AppScreen.CHAT)
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isImeVisible) {
                BidiokBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            when (currentScreen) {
                AppScreen.CHAT -> {
                    val liveSearchStatus by viewModel.liveSearchStatus.collectAsState()
                    ChatScreen(
                        messages = chatMessages,
                        selectedModel = selectedModel,
                        onModelSelected = { viewModel.selectModel(it) },
                        onSendMessage = { prompt, reasoning, webSearch, attachText, attachUri ->
                            viewModel.sendMessage(prompt, reasoning, webSearch, attachText, attachUri)
                        },
                        onClearChat = { viewModel.clearChat() },
                        onNavigateTo3D = { prompt ->
                            viewModel.navigateTo3DWithPrompt(prompt)
                        },
                        voiceManager = viewModel.voiceManager,
                        isProcessing = isAiProcessing,
                        liveSearchStatus = liveSearchStatus
                    )
                }

                AppScreen.QR_SCANNER -> {
                    QrScannerScreen(
                        historyList = qrHistory,
                        onSaveScannedRecord = { raw, type, analysis ->
                            viewModel.saveQrRecord(raw, type, analysis)
                        },
                        onDeleteRecord = { record ->
                            viewModel.deleteQrRecord(record)
                        },
                        onAskAiAboutQr = { query ->
                            viewModel.navigateTo(AppScreen.CHAT)
                            viewModel.sendMessage(query, deepReasoning = true, webSearch = true)
                        }
                    )
                }

                AppScreen.THREE_D_STUDIO -> {
                    ThreeDStudioScreen(
                        initialPrompt = active3DPrompt,
                        onSaveModel = { model ->
                            viewModel.save3DModel(model)
                        }
                    )
                }

                AppScreen.CREATIVE_HUB -> {
                    CreativeHubScreen(
                        onSaveCraft = { craft ->
                            viewModel.saveCraft(craft)
                        }
                    )
                }

                AppScreen.PROFILE -> {
                    val userMemories by viewModel.userMemories.collectAsState()
                    val geminiKey by viewModel.geminiApiKey.collectAsState()
                    val openAiKey by viewModel.openAiApiKey.collectAsState()
                    val claudeKey by viewModel.claudeApiKey.collectAsState()
                    val deepseekKey by viewModel.deepseekApiKey.collectAsState()
                    val groqKey by viewModel.groqApiKey.collectAsState()
                    ProfileScreen(
                        historyList = activityHistory,
                        memoriesList = userMemories,
                        geminiApiKey = geminiKey,
                        openAiApiKey = openAiKey,
                        claudeApiKey = claudeKey,
                        deepseekApiKey = deepseekKey,
                        groqApiKey = groqKey,
                        onSaveGeminiApiKey = { viewModel.updateGeminiApiKey(it) },
                        onSaveOpenAiApiKey = { viewModel.updateOpenAiApiKey(it) },
                        onSaveClaudeApiKey = { viewModel.updateClaudeApiKey(it) },
                        onSaveDeepseekApiKey = { viewModel.updateDeepseekApiKey(it) },
                        onSaveGroqApiKey = { viewModel.updateGroqApiKey(it) },
                        onDeleteHistoryItem = { item ->
                            viewModel.deleteHistoryItem(item)
                        },
                        onClearAllHistory = {
                            viewModel.clearAllHistory()
                        },
                        onDeleteMemory = { memory ->
                            viewModel.deleteUserMemory(memory)
                        },
                        onAddMemory = { topic, content ->
                            viewModel.addUserMemory(topic, content)
                        },
                        onSelectHistoryQuery = { query ->
                            viewModel.navigateTo(AppScreen.CHAT)
                            viewModel.sendMessage(query, deepReasoning = true, webSearch = true)
                        }
                    )
                }
            }
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BidiokBottomNavigation(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    val items = listOf(
        NavItem(AppScreen.CHAT, "Assistant", Icons.Default.Chat, "nav_chat"),
        NavItem(AppScreen.QR_SCANNER, "Vision/QR", Icons.Default.QrCodeScanner, "nav_qr"),
        NavItem(AppScreen.THREE_D_STUDIO, "3D Studio", Icons.Default.ViewInAr, "nav_3d"),
        NavItem(AppScreen.CREATIVE_HUB, "Creative", Icons.Default.AutoAwesome, "nav_creative"),
        NavItem(AppScreen.PROFILE, "Profile", Icons.Default.Person, "nav_profile")
    )

    NavigationBar(
        containerColor = CyberSurface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = CyberBorder
        )
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberBackground,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
