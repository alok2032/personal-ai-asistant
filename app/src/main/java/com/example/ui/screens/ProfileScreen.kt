package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SearchHistoryRecord
import com.example.data.model.UserMemoryRecord
import com.example.ui.components.GlowingCyberCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.SpaceStarsBackground
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    historyList: List<SearchHistoryRecord>,
    memoriesList: List<UserMemoryRecord>,
    geminiApiKey: String = "",
    openAiApiKey: String = "",
    claudeApiKey: String = "",
    deepseekApiKey: String = "",
    groqApiKey: String = "",
    onSaveGeminiApiKey: (String) -> Unit = {},
    onSaveOpenAiApiKey: (String) -> Unit = {},
    onSaveClaudeApiKey: (String) -> Unit = {},
    onSaveDeepseekApiKey: (String) -> Unit = {},
    onSaveGroqApiKey: (String) -> Unit = {},
    onDeleteHistoryItem: (SearchHistoryRecord) -> Unit,
    onClearAllHistory: () -> Unit,
    onDeleteMemory: (UserMemoryRecord) -> Unit,
    onAddMemory: (String, String) -> Unit,
    onSelectHistoryQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isGoogleLinked by remember { mutableStateOf(true) }
    var userEmail by remember { mutableStateOf("alokchandrabhat@gmail.com") }
    var userName by remember { mutableStateOf("Alok Chandra Bhat") }

    var selectedKeyTab by remember { mutableIntStateOf(0) } // 0: OpenAI, 1: Gemini, 2: Claude, 3: DeepSeek, 4: Groq
    var openAiKeyInput by remember(openAiApiKey) { mutableStateOf(openAiApiKey) }
    var geminiKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var claudeKeyInput by remember(claudeApiKey) { mutableStateOf(claudeApiKey) }
    var deepseekKeyInput by remember(deepseekApiKey) { mutableStateOf(deepseekApiKey) }
    var groqKeyInput by remember(groqApiKey) { mutableStateOf(groqApiKey) }

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Search History, 1: AI Personal Memories
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showClearConfirmation by remember { mutableStateOf(false) }

    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var newMemoryTopic by remember { mutableStateOf("") }
    var newMemoryContent by remember { mutableStateOf("") }

    val filteredHistory = remember(historyList, searchQuery, selectedCategoryFilter) {
        historyList.filter { record ->
            val matchesSearch = searchQuery.isBlank() || record.query.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "ALL" || record.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }
    }

    SpaceStarsBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyberBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Profile & Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Google Account, Memory & Activity",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                NeonBadge(
                    text = if (isGoogleLinked) "LINKED" else "UNLINKED",
                    color = if (isGoogleLinked) NeonEmerald else NeonRose
                )
            }

            // Google Account Integration Card
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isGoogleLinked) NeonEmerald.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CyberSurface)
                                    .border(1.dp, if (isGoogleLinked) NeonEmerald else CyberBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGoogleLinked) "G" else "?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGoogleLinked) NeonEmerald else TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isGoogleLinked) userName else "Guest Profile",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isGoogleLinked) userEmail else "Link Google Account to sync across devices",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isGoogleLinked = !isGoogleLinked
                                val msg = if (isGoogleLinked) "Linked with Google Account" else "Unlinked Google Account"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("toggle_google_link_button")
                        ) {
                            Icon(
                                imageVector = if (isGoogleLinked) Icons.Default.LinkOff else Icons.Default.Link,
                                contentDescription = "Toggle Google Link",
                                tint = if (isGoogleLinked) NeonEmerald else NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quantum Memory & Sync Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${memoriesList.size} Facts • ${historyList.size} Searches",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live AI API Key Multi-Model Settings Card
            GlowingCyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (openAiKeyInput.isNotBlank() || geminiKeyInput.isNotBlank() || claudeKeyInput.isNotBlank()) NeonCyan.copy(alpha = 0.5f) else CyberBorder
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live AI Engine Keys",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        val hasAnyKey = openAiKeyInput.isNotBlank() || geminiKeyInput.isNotBlank() || claudeKeyInput.isNotBlank()
                        NeonBadge(
                            text = if (hasAnyKey) "LIVE KEY ACTIVE" else "HYBRID CONSENSUS",
                            color = if (hasAnyKey) NeonEmerald else NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Key Model Selector Tabs (5 Flagship Engines)
                    val keyTabs = listOf("OpenAI", "Gemini", "Claude", "DeepSeek", "Groq")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurface)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        keyTabs.forEachIndexed { idx, title ->
                            val isTabSelected = selectedKeyTab == idx
                            val isKeyConfigured = when (idx) {
                                0 -> openAiKeyInput.isNotBlank()
                                1 -> geminiKeyInput.isNotBlank()
                                2 -> claudeKeyInput.isNotBlank()
                                3 -> deepseekKeyInput.isNotBlank()
                                else -> groqKeyInput.isNotBlank()
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTabSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isTabSelected) NeonCyan else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedKeyTab = idx }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isKeyConfigured) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(NeonEmerald)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isTabSelected) NeonCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val (currentKeyVal, placeholderText, helperDesc) = when (selectedKeyTab) {
                        0 -> Triple(
                            openAiKeyInput,
                            "Paste OpenAI key (sk-...)",
                            "Connects ChatGPT Astra directly to OpenAI GPT-4o live API."
                        )
                        1 -> Triple(
                            geminiKeyInput,
                            "Paste Gemini key (AIzaSy...)",
                            "Connects Gemini Live 2.5 Flash for high-speed multimodal reasoning."
                        )
                        2 -> Triple(
                            claudeKeyInput,
                            "Paste Anthropic key (sk-ant-...)",
                            "Connects Claude Mythic 3.5 for deep analytical reasoning."
                        )
                        3 -> Triple(
                            deepseekKeyInput,
                            "Paste DeepSeek key (sk-...)",
                            "Connects DeepSeek R1/V3 for advanced reasoning and logic."
                        )
                        else -> Triple(
                            groqKeyInput,
                            "Paste Groq key (gsk_...)",
                            "Connects Groq Llama 3.3 70B for sub-second 500 tokens/sec speed."
                        )
                    }

                    Text(
                        text = helperDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentKeyVal,
                            onValueChange = { newVal ->
                                when (selectedKeyTab) {
                                    0 -> openAiKeyInput = newVal
                                    1 -> geminiKeyInput = newVal
                                    2 -> claudeKeyInput = newVal
                                    3 -> deepseekKeyInput = newVal
                                    4 -> groqKeyInput = newVal
                                }
                            },
                            placeholder = { Text(placeholderText, color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CyberCard,
                                unfocusedContainerColor = CyberCard
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                when (selectedKeyTab) {
                                    0 -> {
                                        onSaveOpenAiApiKey(openAiKeyInput)
                                        val msg = if (openAiKeyInput.isNotBlank()) "OpenAI GPT-4o key activated!" else "OpenAI key cleared"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                    1 -> {
                                        onSaveGeminiApiKey(geminiKeyInput)
                                        val msg = if (geminiKeyInput.isNotBlank()) "Gemini 2.5 key activated!" else "Gemini key cleared"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                    2 -> {
                                        onSaveClaudeApiKey(claudeKeyInput)
                                        val msg = if (claudeKeyInput.isNotBlank()) "Claude 3.5 key activated!" else "Claude key cleared"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                    3 -> {
                                        onSaveDeepseekApiKey(deepseekKeyInput)
                                        val msg = if (deepseekKeyInput.isNotBlank()) "DeepSeek R1 key activated!" else "DeepSeek key cleared"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                    4 -> {
                                        onSaveGroqApiKey(groqKeyInput)
                                        val msg = if (groqKeyInput.isNotBlank()) "Groq LPU key activated!" else "Groq key cleared"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Tab Row: Activity History vs Personal Memories
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = CyberSurface.copy(alpha = 0.8f),
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = if (selectedSection == 0) NeonPurple else NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Search & Tasks (${historyList.size})", fontSize = 11.sp, color = if (selectedSection == 0) NeonPurple else TextSecondary) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("AI Personal Memories (${memoriesList.size})", fontSize = 11.sp, color = if (selectedSection == 1) NeonCyan else TextSecondary) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedSection) {
                0 -> {
                    // Search & Tasks History
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Filter past queries & tasks...", color = TextMuted, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("history_search_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CyberCard,
                                    unfocusedContainerColor = CyberCard
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (historyList.isNotEmpty()) {
                                IconButton(
                                    onClick = { showClearConfirmation = true },
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = NeonRose)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Filter Chips
                        val categories = listOf("ALL", "CHAT", "3D_STUDIO", "QR", "IMAGE", "VIDEO", "DOCUMENT")
                        val chipScroll = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(chipScroll),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSel = cat == selectedCategoryFilter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) NeonPurple.copy(alpha = 0.25f) else CyberSurfaceVariant)
                                        .border(1.dp, if (isSel) NeonPurple else CyberBorder, RoundedCornerShape(10.dp))
                                        .clickable { selectedCategoryFilter = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSel) NeonPurple else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No matching records found" else "No search or task records yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredHistory, key = { it.id }) { record ->
                                    HistoryRecordItem(
                                        record = record,
                                        onDelete = { onDeleteHistoryItem(record) },
                                        onSelect = { onSelectHistoryQuery(record.query) }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Personal AI Memories Section
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "What Bidiok AI Remembers About You:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )

                            Button(
                                onClick = { showAddMemoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Fact", fontSize = 11.sp, color = CyberBackground, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (memoriesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No personal memories stored yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text(
                                        text = "Tell the AI about yourself in chat (e.g., 'My name is Alex', 'My project is Quantum Drone'), and it will remember here forever!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(memoriesList, key = { it.id }) { memory ->
                                    GlowingCyberCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = NeonCyan.copy(alpha = 0.35f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    NeonBadge(text = memory.keyTopic, color = NeonCyan)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Active in AI Recall",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = NeonEmerald,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = memory.content,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteMemory(memory) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete memory", tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Memory Dialog
        if (showAddMemoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddMemoryDialog = false },
                containerColor = CyberCard,
                title = { Text("Remember Personal Fact", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newMemoryTopic,
                            onValueChange = { newMemoryTopic = it },
                            placeholder = { Text("Topic (e.g., Name, Project, Location)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newMemoryContent,
                            onValueChange = { newMemoryContent = it },
                            placeholder = { Text("Fact details (e.g., I am working on autonomous robotics)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMemoryTopic.isNotBlank() && newMemoryContent.isNotBlank()) {
                                onAddMemory(newMemoryTopic, newMemoryContent)
                                newMemoryTopic = ""
                                newMemoryContent = ""
                                showAddMemoryDialog = false
                                Toast.makeText(context, "Memory stored in AI vault", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Save Fact", color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddMemoryDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Confirmation Alert Dialog for Clearing All History
        if (showClearConfirmation) {
            AlertDialog(
                onDismissRequest = { showClearConfirmation = false },
                containerColor = CyberCard,
                title = {
                    Text(
                        text = "Clear Entire History Vault?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete all stored search, 3D modeling, and activity history? This action cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllHistory()
                            showClearConfirmation = false
                            Toast.makeText(context, "All history cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRose)
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmation = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryRecordItem(
    record: SearchHistoryRecord,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    val formattedTime = remember(record.timestamp) {
        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
    }

    val badgeColor = when (record.category) {
        "3D_STUDIO" -> NeonCyan
        "QR" -> NeonEmerald
        "IMAGE" -> NeonPurple
        "VIDEO" -> NeonRose
        "DOCUMENT" -> NeonAmber
        else -> NeonCyan
    }

    GlowingCyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("history_item_${record.id}"),
        borderColor = CyberBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonBadge(text = record.category.replace("_", " "), color = badgeColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.query,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 2
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_history_${record.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete record",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

