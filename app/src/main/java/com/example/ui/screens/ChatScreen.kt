package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AiModelType
import com.example.data.model.ChatMessage
import com.example.data.service.FileConverterEngine
import com.example.data.service.PdfGeneratorEngine
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.ModelSelectorBar
import com.example.ui.components.NeonBadge
import com.example.ui.components.SpaceStarsBackground
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.VoiceAssistantManager

data class AttachedMediaItem(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val isImage: Boolean,
    val isVideo: Boolean,
    val isDoc: Boolean,
    val textContent: String? = null
)

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    selectedModel: AiModelType,
    onModelSelected: (AiModelType) -> Unit,
    onSendMessage: (String, Boolean, Boolean, String?, String?) -> Unit,
    onClearChat: () -> Unit,
    onNavigateTo3D: (String) -> Unit,
    voiceManager: VoiceAssistantManager,
    isProcessing: Boolean,
    liveSearchStatus: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var attachedItem by remember { mutableStateOf<AttachedMediaItem?>(null) }
    var showAttachmentPicker by remember { mutableStateOf(false) }

    val isListening by voiceManager.isListening.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val audioAmp by voiceManager.audioAmplitude.collectAsState()
    val speechHeard by voiceManager.lastSpeechText.collectAsState()

    // 1. Photo Picker Launcher (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
            attachedItem = AttachedMediaItem(
                uri = uri,
                name = fileName,
                mimeType = "image/*",
                isImage = true,
                isVideo = false,
                isDoc = false
            )
            Toast.makeText(context, "Attached: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Video Picker Launcher (zero-permission)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "video_${System.currentTimeMillis()}.mp4"
            attachedItem = AttachedMediaItem(
                uri = uri,
                name = fileName,
                mimeType = "video/*",
                isImage = false,
                isVideo = true,
                isDoc = false
            )
            Toast.makeText(context, "Attached: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Document / File Picker Launcher
    val docPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "document.txt"
            val textContent = FileConverterEngine.readTextFromUri(context, uri)
            attachedItem = AttachedMediaItem(
                uri = uri,
                name = fileName,
                mimeType = "application/octet-stream",
                isImage = false,
                isVideo = false,
                isDoc = true,
                textContent = textContent
            )
            Toast.makeText(context, "Attached: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Fallback Speech Intent Launcher for devices/emulators without native SpeechRecognizer service
    val speechIntentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                inputText = spoken
                voiceManager.handleExternalSpeechResult(spoken)
                onSendMessage(spoken, true, true, null, null)
            }
        }
    }

    val triggerSpeechIntent: () -> Unit = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Bidiok AI in any language...")
            }
            speechIntentLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice recognition service unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    val startVoiceInput: () -> Unit = {
        if (voiceManager.isRecognitionAvailable()) {
            try {
                voiceManager.startListening()
            } catch (e: Exception) {
                triggerSpeechIntent()
            }
        } else {
            triggerSpeechIntent()
        }
    }

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Microphone enabled", Toast.LENGTH_SHORT).show()
            startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission required for voice commands", Toast.LENGTH_LONG).show()
        }
    }

    val onToggleVoice: () -> Unit = {
        if (isListening) {
            voiceManager.stopListening()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                startVoiceInput()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Scroll to bottom when message arrives, isProcessing changes, or software keyboard appears
    LaunchedEffect(messages.size, isProcessing, isImeVisible) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll when typing so current messages and live query remain in full view above keyboard
    LaunchedEffect(inputText) {
        if (messages.isNotEmpty() && inputText.isNotBlank()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-update input with spoken words
    LaunchedEffect(speechHeard) {
        if (speechHeard.isNotBlank() && isListening) {
            inputText = speechHeard
        }
    }

    SpaceStarsBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface.copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bidiok AI",
                            tint = CyberBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bidiok AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                        }
                        Text(
                            text = "Universal Multi-Model AI Consensus",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Chat",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Model Switcher Bar
            ModelSelectorBar(
                selectedModel = selectedModel,
                onModelSelected = onModelSelected,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Voice Status Waveform Banner
            AnimatedVisibility(
                visible = isListening || isSpeaking,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isListening) NeonRose.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AudioWaveformVisualizer(
                            amplitude = audioAmp,
                            isActive = isListening || isSpeaking,
                            barCount = 7
                        )
                        Text(
                            text = if (isListening) "Listening: $speechHeard" else "Bidiok Voice Speaking…",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isListening) voiceManager.stopListening()
                            if (isSpeaking) voiceManager.stopSpeaking()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Voice",
                            tint = NeonRose
                        )
                    }
                }
            }

            // Message Body or Clean Space Orb
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    CleanCosmicFront(
                        isListening = isListening,
                        onMicTap = onToggleVoice,
                        inputText = inputText
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            CleanChatMessageBubble(
                                message = msg,
                                onSpeak = { voiceManager.speak(msg.content) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Bidiok AI", msg.content))
                                    Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                                },
                                onOpen3D = { onNavigateTo3D(msg.content) },
                                onExportPdf = {
                                    val pdfFile = PdfGeneratorEngine.createDocumentPdf(
                                        context = context,
                                        title = if (msg.role == "user") "Query Export" else "Bidiok AI Intelligence Brief",
                                        content = msg.content
                                    )
                                    PdfGeneratorEngine.sharePdf(context, pdfFile)
                                }
                            )
                        }

                        if (isProcessing) {
                            item {
                                CleanProcessingIndicator(
                                    model = selectedModel,
                                    statusText = liveSearchStatus
                                )
                            }
                        }
                    }
                }
            }

            // Live Search & Reasoning Transparency HUD (above bottom bar)
            AnimatedVisibility(
                visible = isProcessing && !liveSearchStatus.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant.copy(alpha = 0.95f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = NeonCyan,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = liveSearchStatus ?: "Thinking...",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Attachment Preview Chip (above input bar if file is attached)
            if (attachedItem != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard)
                        .border(1.dp, NeonEmerald.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val fileIcon = when {
                            attachedItem!!.isImage -> Icons.Default.Image
                            attachedItem!!.isVideo -> Icons.Default.Movie
                            else -> Icons.Default.Description
                        }
                        Icon(
                            imageVector = fileIcon,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = attachedItem!!.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = if (attachedItem!!.isDoc) "Document text ready for synthesis" else "Media ready for multimodal upload",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { attachedItem = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Live Search & Typing Elevation Bar: Displays real-time query shifting upward so what is being typed is clearly visible above keyboard
            AnimatedVisibility(
                visible = inputText.isNotBlank(),
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = CyberCard.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Text(
                                    text = "LIVE SEARCH QUERY / সার্চ টাইপিং",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "${inputText.length} chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large, high-visibility typed text display
                        Text(
                            text = inputText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick-execution multi-AI actions based on the query
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LiveQueryActionChip(
                                label = "Web Search",
                                icon = Icons.Default.Language,
                                tint = NeonCyan,
                                onClick = {
                                    val query = inputText.trim()
                                    val item = attachedItem
                                    inputText = ""
                                    attachedItem = null
                                    onSendMessage(query, true, true, item?.textContent, item?.uri?.toString())
                                }
                            )
                            LiveQueryActionChip(
                                label = "3D CAD Model",
                                icon = Icons.Default.ViewInAr,
                                tint = NeonPurple,
                                onClick = {
                                    val query = inputText.trim()
                                    inputText = ""
                                    onSendMessage("Generate a complete 3D OBJ model for: $query", true, false, null, null)
                                }
                            )
                            LiveQueryActionChip(
                                label = "Draw Picture",
                                icon = Icons.Default.Image,
                                tint = NeonRose,
                                onClick = {
                                    val query = inputText.trim()
                                    inputText = ""
                                    onSendMessage("Create a high-resolution photorealistic image of: $query", false, false, null, null)
                                }
                            )
                            LiveQueryActionChip(
                                label = "Create PDF",
                                icon = Icons.Default.PictureAsPdf,
                                tint = NeonEmerald,
                                onClick = {
                                    val query = inputText.trim()
                                    inputText = ""
                                    onSendMessage("Generate an exhaustive deep document and report on: $query", true, true, null, null)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Bar: Attachment Button + Mic + Text Input + Send
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attachment Button
                IconButton(
                    onClick = { showAttachmentPicker = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberCard)
                        .border(1.dp, if (attachedItem != null) NeonEmerald else CyberBorder, CircleShape)
                        .testTag("chat_attachment_button")
                ) {
                    Icon(
                        imageVector = if (attachedItem != null) Icons.Default.AttachFile else Icons.Default.Add,
                        contentDescription = "Upload Photo, Video or File",
                        tint = if (attachedItem != null) NeonEmerald else NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Voice Mic Button
                val infiniteTransition = rememberInfiniteTransition(label = "MicGlow")
                val micScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (isListening) 1.15f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "micScale"
                )

                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .size(44.dp)
                        .scale(micScale)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.radialGradient(listOf(NeonRose, Color(0xFF881337)))
                            else Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.35f), CyberSurfaceVariant))
                        )
                        .border(
                            width = if (isListening) 2.dp else 1.dp,
                            color = if (isListening) NeonRose else NeonCyan,
                            shape = CircleShape
                        )
                        .testTag("voice_command_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Command",
                        tint = if (isListening) Color.White else NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Chat Input Field
                val canSend = (inputText.isNotBlank() || attachedItem != null) && !isProcessing
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening..." else "Ask, search, or upload in any language...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Input",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = if (canSend) ImeAction.Send else ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                val query = inputText.trim()
                                val item = attachedItem
                                inputText = ""
                                attachedItem = null
                                onSendMessage(
                                    query,
                                    true,
                                    true,
                                    item?.textContent,
                                    item?.uri?.toString()
                                )
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberCard.copy(alpha = 0.8f),
                        unfocusedContainerColor = CyberCard.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                // Send Button
                IconButton(
                    onClick = {
                        val query = inputText.trim()
                        val item = attachedItem
                        inputText = ""
                        attachedItem = null
                        onSendMessage(
                            query,
                            true,
                            true,
                            item?.textContent,
                            item?.uri?.toString()
                        )
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) NeonCyan else CyberSurfaceVariant)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (canSend) CyberBackground else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Attachment Source Dialog
    if (showAttachmentPicker) {
        AlertDialog(
            onDismissRequest = { showAttachmentPicker = false },
            title = {
                Text("Upload to Bidiok AI", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Select any visual or document format for multi-AI ingestion:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Photo Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Photo / Image", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Vision AI analysis, OCR, art description", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    // 2. Video Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Video Clip", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Scene breakdown, storyboard & transcription", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    // 3. Document / PDF Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                showAttachmentPicker = false
                                docPickerLauncher.launch(arrayOf("*/*"))
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Document / PDF / Code", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Deep text extraction, PDF generation & synthesis", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachmentPicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CleanCosmicFront(
    isListening: Boolean,
    onMicTap: () -> Unit,
    inputText: String = ""
) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val infiniteTransition = rememberInfiniteTransition(label = "FrontOrbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsating Cosmic Neural Core
        val orbSize = if (isImeVisible) 64.dp else 105.dp
        Box(
            modifier = Modifier
                .size(orbSize)
                .scale(if (isListening) 1.2f else pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.35f),
                            NeonPurple.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.5.dp, if (isListening) NeonRose else NeonCyan, CircleShape)
                .clickable { onMicTap() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Bolt,
                contentDescription = "Cosmic Core",
                tint = if (isListening) NeonRose else NeonCyan,
                modifier = Modifier.size(if (isImeVisible) 32.dp else 48.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (isImeVisible) 8.dp else 18.dp))

        Text(
            text = "Bidiok AI",
            style = if (isImeVisible) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (inputText.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                color = CyberCard.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "READY TO SEARCH / সার্চের জন্য প্রস্তুত",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$inputText\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Text(
                text = "Tap the microphone or upload files below",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (!isImeVisible) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "World's flagship AI models consensus • Any language",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun CleanChatMessageBubble(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onOpen3D: () -> Unit,
    onExportPdf: () -> Unit
) {
    val isUser = message.role == "user"
    var isReasoningExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_${message.id}" else "assistant_message_${message.id}"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
                Text(
                    text = "Bidiok Consensus Core (${message.modelType})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) NeonCyan.copy(alpha = 0.18f) else CyberCard.copy(alpha = 0.9f))
                .border(
                    width = 1.dp,
                    color = if (isUser) NeonCyan.copy(alpha = 0.5f) else CyberBorder,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                // Attached Image / Generated Image Preview via Coil
                if (!message.attachedImagePath.isNullOrBlank()) {
                    val path = message.attachedImagePath
                    if (path.startsWith("http") || path.startsWith("content://") || path.endsWith(".jpg") || path.endsWith(".png")) {
                        AsyncImage(
                            model = path,
                            contentDescription = "Visual Attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurface)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attached File Resource", style = MaterialTheme.typography.labelSmall, color = NeonEmerald)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Reasoning matrix
                if (!message.reasoningText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { isReasoningExpanded = !isReasoningExpanded }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Multi-Model Consensus Matrix",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonPurple,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = if (isReasoningExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (isReasoningExpanded) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.reasoningText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Main Content
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 21.sp
                )

                // 3D Model inspect button
                if (message.isCraftResult && message.craftType == "3D") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                            .clickable { onOpen3D() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Inspect 3D Model in Studio (360°)",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Assistant actions (Speak, Copy, Export PDF)
                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Export PDF Action
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .clickable { onExportPdf() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontSize = 10.sp)
                        }

                        Row {
                            IconButton(onClick = onSpeak, modifier = Modifier.size(26.dp)) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CleanProcessingIndicator(
    model: AiModelType,
    statusText: String? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCard.copy(alpha = 0.85f))
            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = NeonCyan,
            strokeWidth = 2.dp
        )
        Text(
            text = statusText ?: "Unifying Universal AI Consensus...",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            maxLines = 1
        )
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return try {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        name ?: uri.path?.substringAfterLast('/')
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LiveQueryActionChip(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
